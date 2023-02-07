import atexit
import logging
import threading
from threading import Thread, Event
from time import sleep

from scavenger.model.CodeBasePublication_pb2 import CodeBasePublication
from scavenger.model.InvocationDataPublication_pb2 import InvocationDataPublication
from scavenger.config import Config
from scavenger.internal.client import Client
from scavenger.internal.invocation_registry import InvocationRegistry
from scavenger.internal.patch import Patcher
from scavenger.internal.scan import CodeBaseScanner
from scavenger.internal.util import current_milli_time

logger = logging.getLogger(__name__)


def start(config: Config):
    Agent(config).start()


class Agent:
    def __init__(self, config: Config):
        self.config = config
        self.client = Client(config.server_url, config.api_key)
        self.invocation_registry = InvocationRegistry()
        self.codebase_scanner = CodeBaseScanner(config.codebase, config.packages, config.exclude_packages)
        self.next_event_milli_time = 0
        self.published_codebase_fingerprint = None
        self.stop_thread_event = Event()

    def start(self):
        logger.info("Scavenger agent is starting.")
        Patcher(self.config.packages, self.invocation_registry).patch()
        if self.config.async_codebase_scan_mode:
            Thread(target=self.send_codebase_publication).start()
        else:
            self.send_codebase_publication()
        Thread(target=self.send_invocation_data_publication_periodically).start()
        Thread(target=self.stop_at_exit, daemon=True).start()

    def stop_at_exit(self):
        # atexit와 main thread join을 통해 main thread가 종료되는 시점을 이중으로 검사

        atexit.register(self.stop_thread_event.set)
        atexit.register(self.invocation_registry.stop_thread_event.set)

        threading.main_thread().join()
        self.stop_thread_event.set()
        self.invocation_registry.stop_thread_event.set()

    def send_codebase_publication(self):
        codebase = self.codebase_scanner.scan()
        self.client.send_codebase_publication(
            CodeBasePublication(
                common_data=self.config.build_common_publication_data(codebase.get_fingerprint(self.config)),
                entry=[function.to_codebase_entry() for function in codebase.functions]
            )
        )
        self.published_codebase_fingerprint = codebase.get_fingerprint(self.config)
        logger.info("Sending codebase publication is Done.")

    def send_invocation_data_publication_periodically(self):
        while self.published_codebase_fingerprint is None and not self.stop_thread_event.is_set():
            sleep(1)

        while not (self.stop_thread_event.is_set() and self.invocation_registry.empty()):
            if current_milli_time() < self.next_event_milli_time:
                sleep(10)
            invocations, recording_interval_started_at_millis = self.invocation_registry.get_invocations()
            self.client.send_invocation_data_publication(
                InvocationDataPublication(
                    common_data=self.config.build_common_publication_data(self.published_codebase_fingerprint),
                    entry=[InvocationDataPublication.InvocationDataEntry(hash=hash_) for hash_ in invocations],
                    recording_interval_started_at_millis=recording_interval_started_at_millis
                )
            )

            self.next_event_milli_time = current_milli_time() + (1000 * 5)
