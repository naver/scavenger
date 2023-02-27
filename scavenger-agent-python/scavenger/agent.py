import logging

from scavenger.config import Config
from scavenger.internal.banner import Banner
from scavenger.internal.client import Client
from scavenger.internal.invocation_registry import InvocationRegistry
from scavenger.internal.patch import Patcher
from scavenger.internal.scan import CodeBaseScanner
from scavenger.internal.scheduler import Scheduler
from scavenger.model.CodeBasePublication_pb2 import CodeBasePublication

logger = logging.getLogger(__name__)


def start(config: Config):
    return Agent(config).start()


class Agent:
    def __init__(self, config: Config):
        self.config = config

        invocation_registry = InvocationRegistry()
        self.codebase_scanner = CodeBaseScanner(config.codebase, config.packages, config.exclude_packages, config.decorators, config.exclude_init)
        self.patcher = Patcher(
            packages=self.config.packages,
            exclude_packages=self.config.exclude_packages,
            decorators=self.config.decorators,
            exclude_init=self.config.exclude_init,
            invocation_registry=invocation_registry
        )
        self.scheduler = Scheduler(
            config=self.config,
            client=Client(self.config),
            codebase_scanner=self.codebase_scanner,
            invocation_registry=invocation_registry
        )

    def start(self):
        Banner(self.config).print()

        codebase = None
        if not self.config.async_codebase_scan_mode:
            codebase = self.codebase_scanner.scan()

        self.patcher.patch()
        if codebase is not None:
            self.scheduler.code_base_publication = CodeBasePublication(
                common_data=self.config.build_common_publication_data(codebase.get_fingerprint(self.config)),
                entry=[function.to_codebase_entry() for function in codebase.functions]
            )
        self.scheduler.start()
        return self.scheduler

    def shutdown(self):
        self.scheduler.shutdown()
