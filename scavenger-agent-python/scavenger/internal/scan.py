import ast
import logging
import os
import time
from _ast import FunctionDef, Module, Call, Attribute, Name
from dataclasses import dataclass
from pathlib import Path
from typing import List

from scavenger.internal.model import Codebase, Function
from scavenger.internal.util import remove_suffix, filter_by_exclude_packages, remove_prefix

logger = logging.getLogger(__name__)


@dataclass
class PyFile:
    codebase_path: Path
    relative_path: Path


class CodeBaseScanner:

    def __init__(self, codebase: List[str], packages: List[str], exclude_packages: List[str], decorators: List[str], exclude_init: bool):
        self.codebase_path_list = [Path(codebase_path) for codebase_path in codebase]
        self.packages = packages
        self.exclude_packages = exclude_packages
        self.decorators = decorators
        self.exclude_init = exclude_init

    def scan(self) -> Codebase:
        logger.info("Codebase scanning is starting.")
        start: int = time.perf_counter_ns()
        functions: List[Function] = []

        for py_file in self.find_all_py_files():
            with open(py_file.codebase_path.joinpath(py_file.relative_path), "r") as r:
                root: Module = ast.parse(r.read())

            package: str = remove_suffix(str(py_file.relative_path).replace(os.sep, "."), ".py")

            function_nodes: List[FunctionDef] = self.get_all_functions_from_tree(root)

            for functionDef in function_nodes:
                functions.append(self.function_def_to_function(functionDef, package))

        logger.info(
            f"Codebase scanning is done. Found {len(functions)} functions. {int((time.perf_counter_ns() - start) / 1_000_000)}ms elapsed")

        return Codebase(functions=functions)

    def get_all_functions_from_tree(self, root: Module) -> List[FunctionDef]:
        function_nodes: List[FunctionDef] = []

        for node in ast.walk(root):
            if isinstance(node, ast.ClassDef):
                for child in ast.iter_child_nodes(node):
                    child._parent = remove_prefix(f"{getattr(node, '_parent', '')}.{node.name}", ".")
            elif isinstance(node, ast.FunctionDef):
                if self.exclude_init and node.name == '__init__':
                    continue

                if self.decorators and not self.has_decorator(self.get_decorators(node)):
                    continue

                function_nodes.append(node)
        return function_nodes

    def has_decorator(self, decorators):
        return sum(1 for decorator in decorators if decorator in self.decorators) >= 1

    @staticmethod
    def get_decorators(function_def: ast.FunctionDef):
        decorators = []
        for decorator in function_def.decorator_list:
            decorators.append(f"@{get_decorator_from_node(decorator)}")
        return decorators

    @staticmethod
    def function_def_to_function(function: FunctionDef, package: str) -> Function:
        parameter_types: List[str] = [arg.arg for arg in function.args.args]

        if function.args.vararg is not None:
            parameter_types.append(f"*{function.args.vararg.arg}")
        if function.args.kwarg is not None:
            parameter_types.append(f"**{function.args.kwarg.arg}")

        parameter_types_str = ", ".join(parameter_types)
        declaring_type = remove_suffix(f"{package}.{getattr(function, '_parent', '')}", ".")

        return Function(
            declaring_type=declaring_type,
            name=function.name,
            parameter_types=parameter_types_str,
            signature=f"{declaring_type}.{function.name}({parameter_types_str})",
            package_name=package
        )

    def find_all_py_files(self) -> List[PyFile]:
        py_files: List[PyFile] = []

        for codebase_path in self.codebase_path_list:
            exclude_packages = [str(codebase_path.joinpath(exclude_package.replace(".", os.sep))) for exclude_package in
                                self.exclude_packages]

            for package in self.packages:
                py_files += self.find_files_in_package(codebase_path, exclude_packages, package)

        return py_files

    @staticmethod
    def find_files_in_package(codebase_path, exclude_packages, package):
        py_files = []
        pattern: str = os.path.join(package.replace(".", os.sep), "**", "*.py")
        for absolute_path in codebase_path.glob(pattern):
            if not filter_by_exclude_packages(str(absolute_path), exclude_packages):
                py_files.append(PyFile(codebase_path, absolute_path.relative_to(codebase_path)))
        return py_files


def get_decorator_from_node(node):
    if isinstance(node, Name):
        return node.id
    elif isinstance(node, Call):
        return f"{get_decorator_from_node(node.func)}"
    elif isinstance(node, Attribute):
        return f"{get_decorator_from_node(node.value)}.{node.attr}"
    else:
        raise logger.warning(f"Unknown decorator type : {node}")
