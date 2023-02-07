from scavenger.model import CommonPublicationData_pb2 as _CommonPublicationData_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class InvocationDataPublication(_message.Message):
    __slots__ = ["common_data", "entry", "recording_interval_started_at_millis"]
    class InvocationDataEntry(_message.Message):
        __slots__ = ["hash"]
        HASH_FIELD_NUMBER: _ClassVar[int]
        hash: str
        def __init__(self, hash: _Optional[str] = ...) -> None: ...
    COMMON_DATA_FIELD_NUMBER: _ClassVar[int]
    ENTRY_FIELD_NUMBER: _ClassVar[int]
    RECORDING_INTERVAL_STARTED_AT_MILLIS_FIELD_NUMBER: _ClassVar[int]
    common_data: _CommonPublicationData_pb2.CommonPublicationData
    entry: _containers.RepeatedCompositeFieldContainer[InvocationDataPublication.InvocationDataEntry]
    recording_interval_started_at_millis: int
    def __init__(self, common_data: _Optional[_Union[_CommonPublicationData_pb2.CommonPublicationData, _Mapping]] = ..., entry: _Optional[_Iterable[_Union[InvocationDataPublication.InvocationDataEntry, _Mapping]]] = ..., recording_interval_started_at_millis: _Optional[int] = ...) -> None: ...
