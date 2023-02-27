import logging
from datetime import datetime, timedelta
from typing import Optional

from apscheduler.schedulers.background import BackgroundScheduler
from grpc import StatusCode
from grpc._channel import _InactiveRpcError

from scavenger.config import Config
from scavenger.internal.client import Client
from scavenger.internal.invocation_registry import InvocationRegistry
from scavenger.internal.model import SchedulerState
from scavenger.internal.scan import CodeBaseScanner
from scavenger.model.CodeBasePublication_pb2 import CodeBasePublication
from scavenger.model.InvocationDataPublication_pb2 import InvocationDataPublication

logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.DEBUG)


class Scheduler:
    def __init__(self, config: Config, client: Client, codebase_scanner: CodeBaseScanner, invocation_registry: InvocationRegistry):
        self.config = config
        self.client = client
        self.codebase_scanner = codebase_scanner
        self.invocation_registry = invocation_registry

        self.scheduler = BackgroundScheduler(daemon=False)
        self.scheduler.add_job(self.run,
                               "interval",
                               seconds=self.config.scheduler_interval_seconds,
                               next_run_time=datetime.now() + timedelta(seconds=self.config.scheduler_initial_delay_seconds))
        self.is_code_base_published = False
        self.code_base_publication: Optional[CodeBasePublication] = None
        self.invocation_data_publication: Optional[InvocationDataPublication] = None
        self.code_base_fingerprint = None
        self.dynamic_config = None
        interval_seconds = 600
        retry_interval_seconds = 600
        self.force_interval_seconds = self.config.force_interval_seconds
        self.poll_state = SchedulerState("config_pool").initialize(interval_seconds, retry_interval_seconds)
        self.code_base_publisher_state = SchedulerState("codebase").initialize(interval_seconds, retry_interval_seconds)
        self.invocation_data_publisher_state = SchedulerState("invocation_data").initialize(interval_seconds, retry_interval_seconds)

        if self.force_interval_seconds != 0:
            self.poll_state.update_intervals(self.force_interval_seconds, self.force_interval_seconds)
            self.code_base_publisher_state.update_intervals(self.force_interval_seconds, self.force_interval_seconds)
            self.invocation_data_publisher_state.update_intervals(self.force_interval_seconds, self.force_interval_seconds)

    def start(self):
        logging.info("Scheduler is starting")
        self.scheduler.start()

    def run(self):
        logging.debug("Task is starting")
        self.poll_dynamic_config_if_needed()
        self.publish_code_base_if_needed()
        self.publish_invocation_data_if_needed()
        logging.debug("Task is done")

    def shutdown(self):
        self.scheduler.shutdown()
        if self.dynamic_config is not None:
            self.code_base_publisher_state.schedule_now()
            self.publish_code_base_if_needed()

            self.invocation_data_publisher_state.schedule_now()
            self.publish_invocation_data_if_needed()

    def poll_dynamic_config_if_needed(self):
        if self.poll_state.is_due_time():
            logging.debug("Poll dynamic config is due time.")
            # noinspection PyBroadException
            try:
                self.dynamic_config = self.client.poll_config()
                if self.force_interval_seconds == 0:
                    self.poll_state.update_intervals(
                        self.dynamic_config.config_poll_interval_seconds,
                        self.dynamic_config.config_poll_retry_interval_seconds
                    )
                    self.code_base_publisher_state.update_intervals(
                        self.dynamic_config.code_base_publisher_check_interval_seconds,
                        self.dynamic_config.code_base_publisher_retry_interval_seconds
                    )
                    self.invocation_data_publisher_state.update_intervals(
                        self.dynamic_config.invocation_data_publisher_interval_seconds,
                        self.dynamic_config.invocation_data_publisher_retry_interval_seconds
                    )
                self.poll_state.schedule_next()
            except Exception as e:
                if isinstance(e, _InactiveRpcError):
                    if e.code() == StatusCode.UNAUTHENTICATED:
                        logging.warning("Authentication is Failed.")
                        self.shutdown()
                logging.warning(e)
                self.poll_state.schedule_retry()

    def publish_code_base_if_needed(self):
        if not self.is_code_base_published and self.code_base_publisher_state.is_due_time() and self.dynamic_config is not None:
            logging.debug("Publish codebase is due time.")
            try:
                codebase = self.codebase_scanner.scan()

                if len(codebase.functions) > 100_000:
                    logging.warning("maximum methods count(100000) exceed", len(codebase.functions))
                    return

                if len(codebase.functions) == 0:
                    logging.warning("no methods are found")
                    return

                self.code_base_publication = CodeBasePublication(
                    common_data=self.config.build_common_publication_data(codebase.get_fingerprint(self.config)),
                    entry=[function.to_codebase_entry() for function in codebase.functions]
                )

                self.client.send_codebase_publication(self.code_base_publication)
                self.is_code_base_published = True
                self.code_base_fingerprint = self.code_base_publication.common_data.code_base_fingerprint
                self.code_base_publication = None
            except Exception as e:
                logging.warning(e)
                self.code_base_publisher_state.schedule_retry()

    def publish_invocation_data_if_needed(self):
        if self.invocation_data_publisher_state.is_due_time() and self.dynamic_config is not None and self.is_code_base_published:
            logging.debug("Publish invocation data is due time.")
            try:
                if self.invocation_data_publication is None:
                    invocations, recording_interval_started_at_millis = self.invocation_registry.get_invocations()
                    self.invocation_data_publication = InvocationDataPublication(
                        common_data=self.config.build_common_publication_data(self.code_base_fingerprint),
                        entry=[InvocationDataPublication.InvocationDataEntry(hash=hash_) for hash_ in invocations],
                        recording_interval_started_at_millis=recording_interval_started_at_millis
                    )
                self.client.send_invocation_data_publication(
                    self.invocation_data_publication
                )
                self.invocation_data_publication = None
                self.invocation_data_publisher_state.schedule_next()
            except Exception as e:
                logging.warning(e)
                self.invocation_data_publisher_state.schedule_retry()
