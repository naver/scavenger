import ast
import logging
import os
import time
from _ast import FunctionDef, Module
from dataclasses import dataclass
from pathlib import Path

from scavenger.internal.model import Codebase, Function

logger = logging.getLogger(__name__)


@dataclass
class PyFile:
    codebase_path: Path
    relative_path: Path


class CodeBaseScanner:
    packages: list[str]
    codebase_path_list: list[Path]

    def __init__(self, codebase_path_list: list[Path], packages: list[str], exclude_packages: list[str]):
        self.codebase_path_list = codebase_path_list
        self.packages = packages
        self.exclude_packages = exclude_packages

    def scan(self) -> Codebase:
        logger.info("Codebase scanning is starting.")
        start: int = time.perf_counter_ns()
        functions: list[Function] = []

        for py_file in self.find_all_py_files():
            with open(py_file.codebase_path.joinpath(py_file.relative_path), "r") as r:
                root: Module = ast.parse(r.read())

            package: str = str(py_file.relative_path).replace(os.sep, ".").removesuffix(".py")
            function_nodes: list[FunctionDef] = self.get_all_functions_from_tree(root)

            for functionDef in function_nodes:
                functions.append(self.function_def_to_function(functionDef, package))

        logger.info(
            f"Codebase scanning is done. Found {len(functions)} functions. {int((time.perf_counter_ns() - start) / 1_000_000)}ms elapsed")

        return Codebase(functions=functions)

    @staticmethod
    def get_all_functions_from_tree(root: Module) -> list[FunctionDef]:
        function_nodes: list[FunctionDef] = []

        for node in ast.walk(root):
            if isinstance(node, ast.ClassDef):
                for child in ast.iter_child_nodes(node):
                    child._parent = f"{getattr(node, '_parent', '')}.{node.name}".removeprefix(".")
            elif isinstance(node, ast.FunctionDef):
                function_nodes.append(node)
        return function_nodes

    @staticmethod
    def function_def_to_function(function: FunctionDef, package: str) -> Function:
        parameter_types: list[str] = [arg.arg for arg in function.args.args]

        if function.args.vararg is not None:
            parameter_types.append(f"*{function.args.vararg.arg}")
        if function.args.kwarg is not None:
            parameter_types.append(f"**{function.args.kwarg.arg}")

        parameter_types_str = ", ".join(parameter_types)
        declaring_type = f"{package}.{getattr(function, '_parent', '')}".removesuffix(".")

        return Function(
            declaring_type=declaring_type,
            name=function.name,
            parameter_types=parameter_types_str,
            signature=f"{declaring_type}.{function.name}({parameter_types_str})",
            package_name=package
        )

    def find_all_py_files(self) -> list[PyFile]:
        py_files: list[PyFile] = []

        for codebase_path in self.codebase_path_list:
            exclude_packages = [codebase_path.joinpath(exclude_package.replace(".", os.sep)) for exclude_package in
                                self.exclude_packages]

            for package in self.packages:
                py_files += self.find_files_in_package(codebase_path, exclude_packages, package)

        return py_files

    def find_files_in_package(self, codebase_path, exclude_packages, package):
        py_files = []
        pattern: str = os.path.join(package.replace(".", os.sep), "**", "*.py")
        for absolute_path in codebase_path.glob(pattern):
            if not self.filter_by_exclude_packages(absolute_path, exclude_packages):
                py_files.append(PyFile(codebase_path, absolute_path.relative_to(codebase_path)))
        return py_files

    @staticmethod
    def filter_by_exclude_packages(target, exclude_packages):
        for exclude_package in exclude_packages:
            if target.is_relative_to(exclude_package):
                return True
