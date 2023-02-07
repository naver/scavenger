from scavenger.model import CommonPublicationData_pb2 as _CommonPublicationData_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class CodeBasePublication(_message.Message):
    __slots__ = ["common_data", "entry"]
    class CodeBaseEntry(_message.Message):
        __slots__ = ["declaring_type", "method_name", "modifiers", "package_name", "parameter_types", "signature", "signature_hash", "visibility"]
        DECLARING_TYPE_FIELD_NUMBER: _ClassVar[int]
        METHOD_NAME_FIELD_NUMBER: _ClassVar[int]
        MODIFIERS_FIELD_NUMBER: _ClassVar[int]
        PACKAGE_NAME_FIELD_NUMBER: _ClassVar[int]
        PARAMETER_TYPES_FIELD_NUMBER: _ClassVar[int]
        SIGNATURE_FIELD_NUMBER: _ClassVar[int]
        SIGNATURE_HASH_FIELD_NUMBER: _ClassVar[int]
        VISIBILITY_FIELD_NUMBER: _ClassVar[int]
        declaring_type: str
        method_name: str
        modifiers: str
        package_name: str
        parameter_types: str
        signature: str
        signature_hash: str
        visibility: str
        def __init__(self, declaring_type: _Optional[str] = ..., visibility: _Optional[str] = ..., signature: _Optional[str] = ..., method_name: _Optional[str] = ..., modifiers: _Optional[str] = ..., package_name: _Optional[str] = ..., parameter_types: _Optional[str] = ..., signature_hash: _Optional[str] = ...) -> None: ...
    COMMON_DATA_FIELD_NUMBER: _ClassVar[int]
    ENTRY_FIELD_NUMBER: _ClassVar[int]
    common_data: _CommonPublicationData_pb2.CommonPublicationData
    entry: _containers.RepeatedCompositeFieldContainer[CodeBasePublication.CodeBaseEntry]
    def __init__(self, common_data: _Optional[_Union[_CommonPublicationData_pb2.CommonPublicationData, _Mapping]] = ..., entry: _Optional[_Iterable[_Union[CodeBasePublication.CodeBaseEntry, _Mapping]]] = ...) -> None: ...
