from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Optional as _Optional

DESCRIPTOR: _descriptor.FileDescriptor

class CommonPublicationData(_message.Message):
    __slots__ = ["api_key", "app_name", "app_version", "code_base_fingerprint", "environment", "hostname", "jvm_started_at_millis", "jvm_uuid", "published_at_millis"]
    API_KEY_FIELD_NUMBER: _ClassVar[int]
    APP_NAME_FIELD_NUMBER: _ClassVar[int]
    APP_VERSION_FIELD_NUMBER: _ClassVar[int]
    CODE_BASE_FINGERPRINT_FIELD_NUMBER: _ClassVar[int]
    ENVIRONMENT_FIELD_NUMBER: _ClassVar[int]
    HOSTNAME_FIELD_NUMBER: _ClassVar[int]
    JVM_STARTED_AT_MILLIS_FIELD_NUMBER: _ClassVar[int]
    JVM_UUID_FIELD_NUMBER: _ClassVar[int]
    PUBLISHED_AT_MILLIS_FIELD_NUMBER: _ClassVar[int]
    api_key: str
    app_name: str
    app_version: str
    code_base_fingerprint: str
    environment: str
    hostname: str
    jvm_started_at_millis: int
    jvm_uuid: str
    published_at_millis: int
    def __init__(self, api_key: _Optional[str] = ..., app_name: _Optional[str] = ..., app_version: _Optional[str] = ..., code_base_fingerprint: _Optional[str] = ..., environment: _Optional[str] = ..., hostname: _Optional[str] = ..., jvm_started_at_millis: _Optional[int] = ..., jvm_uuid: _Optional[str] = ..., published_at_millis: _Optional[int] = ...) -> None: ...
