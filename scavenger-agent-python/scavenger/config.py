from dataclasses import dataclass, field
from pathlib import Path
from typing import List

from scavenger.internal.constant import HOSTNAME, JVM_START_AT_MILLIS, PROCESS_UUID
from scavenger.internal.util import current_milli_time
from scavenger.model.CommonPublicationData_pb2 import CommonPublicationData


@dataclass
class Config:
    server_url: str
    api_key: str
    codebase: List[Path]
    packages: List[str]
    exclude_packages: List[str] = field(default_factory=list)
    app_name: str = "missing-appName"
    app_version: str = "unspecified"
    environment: str = "<default>"
    async_codebase_scan_mode: bool = False
    debugMode: bool = False
    scheduler_initial_delay_seconds: int = 10
    scheduler_interval_seconds: int = 10
    force_interval_seconds: int = 0

    def build_common_publication_data(self, codebase_fingerprint) -> CommonPublicationData:
        return CommonPublicationData(
            api_key=self.api_key,
            app_name=self.app_name,
            code_base_fingerprint=codebase_fingerprint,
            environment=self.environment,
            hostname=HOSTNAME,
            jvm_started_at_millis=JVM_START_AT_MILLIS,
            jvm_uuid=PROCESS_UUID,
            app_version=self.app_version,
            published_at_millis=current_milli_time()
        )
