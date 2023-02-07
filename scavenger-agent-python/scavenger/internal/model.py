import hashlib
from dataclasses import dataclass

from typing import List

from scavenger.model.CodeBasePublication_pb2 import CodeBasePublication
from scavenger.config import Config
from scavenger.internal.util import md5


@dataclass
class Function:
    name: str
    declaring_type: str
    signature: str
    parameter_types: str
    package_name: str

    def to_codebase_entry(self) -> CodeBasePublication.CodeBaseEntry:
        return CodeBasePublication.CodeBaseEntry(
            declaring_type=self.declaring_type,
            method_name=self.name,
            modifiers="public",
            package_name=self.package_name,
            parameter_types=self.parameter_types,
            signature=self.signature,
            signature_hash=md5(self.signature),
            visibility="public"
        )


class Codebase:
    functions: List[Function]

    def __init__(self, functions: List[Function]):
        self.functions = functions

    def get_fingerprint(self, config: Config, sort: bool = False):
        m = hashlib.sha256()
        m.update(bytes(str(config.codebase), 'utf-8'))
        m.update(bytes(str(config.packages), 'utf-8'))
        m.update(len(self.functions).to_bytes(1, 'big'))
        functions = sorted(self.functions, key=lambda x: x.name) if sort else self.functions
        for function in functions:
            m.update(bytes(function.signature, 'utf-8'))

        return m.hexdigest()
