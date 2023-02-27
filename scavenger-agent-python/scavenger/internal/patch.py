import inspect
import logging
import sys
from importlib.machinery import PathFinder, SourceFileLoader, ModuleSpec
from typing import Optional, List, Dict

from scavenger.internal.invocation_registry import InvocationRegistry
from scavenger.internal.util import md5, filter_by_exclude_packages


class Finder(PathFinder):
    invocation_registry: InvocationRegistry
    packages: List[str]
    exclude_packages: List[str]

    def __init__(self, packages: List[str], exclude_packages: List[str], decorators: List[str], exclude_init: bool,
                 invocation_registry: InvocationRegistry):
        self.packages = packages
        self.exclude_packages = exclude_packages
        self.decorators = decorators
        self.exclude_init = exclude_init
        self.invocation_registry = invocation_registry

    def find_spec(self, fullname, path=None, target=None) -> ModuleSpec:
        try:
            spec = super().find_spec(fullname, path, target)

            if spec and spec.loader and isinstance(spec.loader, SourceFileLoader) and self.patch_required(fullname):
                loader = ScavengerSourceFileLoader(fullname, spec.origin, self.invocation_registry, self.decorators, self.exclude_init)
                spec.loader = loader
                return spec
        except Exception as e:
            logging.warning("Creating custom loader is failed. ", e)

    def patch_required(self, fullname) -> bool:
        if filter_by_exclude_packages(fullname, self.exclude_packages):
            return False

        return 0 < sum(1 for module_name in self.packages if
                       fullname.startswith(module_name))

    @staticmethod
    def filter_by_exclude_packages(target, exclude_packages):
        for exclude_package in exclude_packages:
            if target.startswith(exclude_package):
                return True


class ScavengerSourceFileLoader(SourceFileLoader):
    invocation_registry: InvocationRegistry

    def __init__(self, fullname, origin, invocation_registry, decorators, exclude_init):
        super().__init__(fullname, origin)
        self.invocation_registry = invocation_registry
        self.decorators = decorators
        self.exclude_init = exclude_init

    def exec_module(self, module):
        super().exec_module(module)
        try:
            self.patch_recursively(module)
        except Exception as e:
            logging.warning("Scavenger function patching is Failed. ", e)

    def patch_recursively(self, obj):
        for key, value in inspect.getmembers(obj):
            if inspect.isfunction(value) or inspect.ismethod(value):
                if not self.is_target_module(value):
                    continue

                if isinstance(inspect.getattr_static(obj, key), classmethod):
                    value = value.__func__

                if self.exclude_init and key == '__init__':
                    continue

                if self.decorators and not self.has_decorator(self.get_decorators(value)):
                    continue

                signature = f"{value.__module__}.{value.__qualname__}{inspect.signature(value)}"

                if isinstance(inspect.getattr_static(obj, key), classmethod):
                    setattr(obj, key, classmethod(self.patch_class_method(value, signature, self.invocation_registry)))
                elif isinstance(inspect.getattr_static(obj, key), staticmethod):
                    setattr(obj, key, staticmethod(self.patch(value, signature, self.invocation_registry)))
                else:
                    setattr(obj, key, self.patch(value, signature, self.invocation_registry))

            elif inspect.isclass(value) and key != "__class__":
                if not self.is_target_module(value):
                    continue

                self.patch_recursively(value)

    def is_target_module(self, child: type) -> bool:
        return child.__module__.startswith(self.name)

    def has_decorator(self, decorators):
        return sum(1 for decorator in decorators if decorator in self.decorators) >= 1

    @staticmethod
    def patch_class_method(function, signature, invocation_registry):
        def wrapper(cls, *args, **kwargs):
            invocation_registry.register(md5(signature))
            return function(cls, *args, **kwargs)

        return wrapper

    @staticmethod
    def patch(function, signature, invocation_registry):
        def wrapper(*args, **kwargs):
            invocation_registry.register(md5(signature))
            return function(*args, **kwargs)

        return wrapper

    @staticmethod
    def get_decorators(func):
        try:
            source = inspect.getsource(func)
        except TypeError as e:
            logging.warning(f"Function inspection error : {func}")
            return []
        index = source.find("def ")
        return [
            line.strip().split()[0].split("(")[0]
            for line in source[:index].strip().splitlines()
            if line.strip()[0] == "@"
        ]


class Patcher:
    _finder: Optional[Finder]
    packages: List[str]
    exclude_packages: List[str]
    store: Dict[str, int]

    def __init__(self, packages: List[str], exclude_packages: List[str], decorators: List[str], exclude_init: bool,
                 invocation_registry: InvocationRegistry):
        self._finder = None
        self.packages = packages
        self.exclude_packages = exclude_packages
        self.decorators = decorators
        self.exclude_init = exclude_init
        self.invocation_registry = invocation_registry

    def patch(self):
        finder = Finder(self.packages, self.exclude_packages, self.decorators, self.exclude_init, self.invocation_registry)
        sys.meta_path.insert(0, finder)
        self._finder = finder

    def unpatch(self):
        sys.meta_path.remove(self._finder)
