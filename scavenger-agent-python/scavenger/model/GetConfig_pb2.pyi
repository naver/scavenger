from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Optional as _Optional

DESCRIPTOR: _descriptor.FileDescriptor

class GetConfigRequest(_message.Message):
    __slots__ = ["api_key", "jvm_uuid"]
    API_KEY_FIELD_NUMBER: _ClassVar[int]
    JVM_UUID_FIELD_NUMBER: _ClassVar[int]
    api_key: str
    jvm_uuid: str
    def __init__(self, api_key: _Optional[str] = ..., jvm_uuid: _Optional[str] = ...) -> None: ...

class GetConfigResponse(_message.Message):
    __slots__ = ["code_base_publisher_check_interval_seconds", "code_base_publisher_retry_interval_seconds", "config_poll_interval_seconds", "config_poll_retry_interval_seconds", "invocation_data_publisher_interval_seconds", "invocation_data_publisher_retry_interval_seconds"]
    CODE_BASE_PUBLISHER_CHECK_INTERVAL_SECONDS_FIELD_NUMBER: _ClassVar[int]
    CODE_BASE_PUBLISHER_RETRY_INTERVAL_SECONDS_FIELD_NUMBER: _ClassVar[int]
    CONFIG_POLL_INTERVAL_SECONDS_FIELD_NUMBER: _ClassVar[int]
    CONFIG_POLL_RETRY_INTERVAL_SECONDS_FIELD_NUMBER: _ClassVar[int]
    INVOCATION_DATA_PUBLISHER_INTERVAL_SECONDS_FIELD_NUMBER: _ClassVar[int]
    INVOCATION_DATA_PUBLISHER_RETRY_INTERVAL_SECONDS_FIELD_NUMBER: _ClassVar[int]
    code_base_publisher_check_interval_seconds: int
    code_base_publisher_retry_interval_seconds: int
    config_poll_interval_seconds: int
    config_poll_retry_interval_seconds: int
    invocation_data_publisher_interval_seconds: int
    invocation_data_publisher_retry_interval_seconds: int
    def __init__(self, config_poll_interval_seconds: _Optional[int] = ..., config_poll_retry_interval_seconds: _Optional[int] = ..., code_base_publisher_check_interval_seconds: _Optional[int] = ..., code_base_publisher_retry_interval_seconds: _Optional[int] = ..., invocation_data_publisher_interval_seconds: _Optional[int] = ..., invocation_data_publisher_retry_interval_seconds: _Optional[int] = ...) -> None: ...
