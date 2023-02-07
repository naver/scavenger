from scavenger.model import GetConfig_pb2 as _GetConfig_pb2
from scavenger.model import CodeBasePublication_pb2 as _CodeBasePublication_pb2
from scavenger.model import InvocationDataPublication_pb2 as _InvocationDataPublication_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Optional as _Optional

DESCRIPTOR: _descriptor.FileDescriptor

class PublicationResponse(_message.Message):
    __slots__ = ["status"]
    STATUS_FIELD_NUMBER: _ClassVar[int]
    status: str
    def __init__(self, status: _Optional[str] = ...) -> None: ...
