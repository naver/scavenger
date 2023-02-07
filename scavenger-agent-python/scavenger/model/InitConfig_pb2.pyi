from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Optional as _Optional

DESCRIPTOR: _descriptor.FileDescriptor

class InitConfigResponse(_message.Message):
    __slots__ = ["collector_url"]
    COLLECTOR_URL_FIELD_NUMBER: _ClassVar[int]
    collector_url: str
    def __init__(self, collector_url: _Optional[str] = ...) -> None: ...
