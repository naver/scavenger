import inspect
import sys
from importlib.machinery import PathFinder, SourceFileLoader, ModuleSpec
from typing import List, Optional

from scavenger.internal.invocation_registry import InvocationRegistry
from scavenger.internal.util import md5


class Finder(PathFinder):
    invocation_registry: InvocationRegistry
    packages: list[str]

    def __init__(self, packages: List[str], invocation_registry: InvocationRegistry):
        self.packages = packages
        self.invocation_registry = invocation_registry

    def find_spec(self, fullname, path=None, target=None) -> ModuleSpec:
        spec = super().find_spec(fullname, path, target)

        if spec and spec.loader and self.patch_required(fullname):
            loader = CustomLoader(fullname, spec.origin, self.invocation_registry)
            spec.loader = loader
            return spec

    def patch_required(self, fullname) -> bool:
        return 0 < sum(1 for module_name in self.packages if fullname.startswith(module_name))


class CustomLoader(SourceFileLoader):
    invocation_registry: InvocationRegistry

    def __init__(self, fullname, origin, invocation_registry):
        super().__init__(fullname, origin)
        self.invocation_registry = invocation_registry

    def exec_module(self, module):
        super().exec_module(module)
        self.patch_recursively(module)

    def patch_recursively(self, obj):
        for child_str in dir(obj):
            child = inspect.getattr_static(obj, child_str)

            is_staticmethod = isinstance(child, staticmethod)

            if not (getattr(child, "__module__", None) is not None and self.is_target_module(child)):
                continue

            if inspect.isfunction(child) or is_staticmethod:
                signature = f"{child.__module__}.{child.__qualname__}{inspect.signature(child)}"
                setattr(obj, child_str, self.patcher(child, signature, self.invocation_registry, is_staticmethod))

            elif inspect.isclass(child) and child is not type:
                self.patch_recursively(child)

    def is_target_module(self, child: type) -> bool:
        return child.__module__.startswith(self.name)

    @staticmethod
    def patcher(function, signature, invocation_registry, is_staticmethod):
        def wrapper(*args, **kwargs):
            invocation_registry.register(md5(signature))
            if is_staticmethod:
                args = args[1:]
            return function(*args, **kwargs)

        return wrapper


class Patcher:
    _finder: Optional[Finder]
    packages: list[str]
    store: dict[str, int]

    def __init__(self, packages: List[str], invocation_registry: InvocationRegistry):
        self._finder = None
        self.packages = packages
        self.invocation_registry = invocation_registry

    def patch(self):
        finder = Finder(self.packages, self.invocation_registry)
        sys.meta_path.insert(0, finder)
        self._finder = finder

    def unpatch(self):
        sys.meta_path.remove(self._finder)
