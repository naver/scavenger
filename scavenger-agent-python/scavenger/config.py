import dataclasses
from configparser import RawConfigParser
from dataclasses import dataclass, field
from pathlib import Path
from typing import List

from dataclass_builder import dataclass_builder

from scavenger.internal.constant import HOSTNAME, PROCESS_START_AT_MILLIS, PROCESS_UUID
from scavenger.internal.util import current_milli_time
from scavenger.model.CommonPublicationData_pb2 import CommonPublicationData


@dataclass
class Config:
    server_url: str
    api_key: str
    codebase: List[str]
    packages: List[str]
    exclude_packages: List[str] = field(default_factory=list)
    decorators: List[str] = field(default_factory=list)
    exclude_init: bool = False
    app_name: str = "missing-appName"
    app_version: str = "unspecified"
    environment: str = "<default>"
    async_codebase_scan_mode: bool = False
    debug_mode: bool = False
    scheduler_initial_delay_seconds: int = 10
    scheduler_interval_seconds: int = 10
    force_interval_seconds: int = 0
    http_connect_timeout_seconds: int = 10
    http_read_timeout_seconds: int = 10

    @staticmethod
    def load_config(file: str = "scavenger.conf"):
        file = Path(file)
        if not file.exists():
            raise FileNotFoundError(f"{file.name} not found")
        with open(file, 'r') as f:
            config_string = '[dummy]\n' + f.read()
        config_from_file = RawConfigParser()
        config_from_file.optionxform = lambda x: x
        config_from_file.read_string(config_string)
        config_from_file = config_from_file["dummy"]

        config_builder = dataclass_builder(Config)()
        aliases_processors = Config.get_aliases_processors()

        for key in config_from_file:
            if key in aliases_processors:
                target_key, processor = aliases_processors[key]
                setattr(config_builder, target_key, processor(config_from_file[key]))

        return config_builder.build()

    @staticmethod
    def get_aliases_processors():
        aliases_processors = {}
        for field_ in dataclasses.fields(Config):
            key = field_.name
            processor = Config.get_processor(field_)
            aliases_processors[key] = (key, processor)

        aliases_processors["scheduler_initial_delay_millis"] = (aliases_processors["scheduler_initial_delay_seconds"][0],
                                                                lambda x: int(aliases_processors["scheduler_initial_delay_seconds"][1](x) / 1000))
        aliases_processors["scheduler_interval_millis"] = (aliases_processors["scheduler_interval_seconds"][0],
                                                           lambda x: int(aliases_processors["scheduler_interval_seconds"][1](x) / 1000))

        # Support camel cases.
        for key, value in list(aliases_processors.items()):
            aliases_processors[(Config.to_camel_case(key))] = value

        return aliases_processors

    @staticmethod
    def get_processor(field_):
        if field_.type == int:
            return lambda x: int(x)
        elif field_.type == bool:
            return lambda x: bool(x)
        elif field_.type == List[str]:
            return lambda x: [attr.strip() for attr in x.split(",")]
        else:
            return lambda x: x

    @staticmethod
    def to_camel_case(snake_str):
        components = snake_str.split('_')

        return components[0] + ''.join(x.title() for x in components[1:])

    def build_common_publication_data(self, codebase_fingerprint) -> CommonPublicationData:
        return CommonPublicationData(
            api_key=self.api_key,
            app_name=self.app_name,
            code_base_fingerprint=codebase_fingerprint,
            environment=self.environment,
            hostname=HOSTNAME,
            jvm_started_at_millis=PROCESS_START_AT_MILLIS,
            jvm_uuid=PROCESS_UUID,
            app_version=self.app_version,
            published_at_millis=current_milli_time()
        )
