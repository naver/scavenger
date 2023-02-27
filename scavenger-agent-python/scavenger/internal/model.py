import hashlib
import math
import time
from dataclasses import dataclass
from typing import List

from scavenger.config import Config
from scavenger.internal.util import md5
from scavenger.model.CodeBasePublication_pb2 import CodeBasePublication


@dataclass
class Function:
    name: str
    declaring_type: str
    signature: str
    parameter_types: str
    package_name: str

    def to_codebase_entry(self) -> CodeBasePublication.CodeBaseEntry:
        return CodeBasePublication.CodeBaseEntry(
            declaring_type=self.declaring_type,
            method_name=self.name,
            modifiers="public",
            package_name=self.package_name,
            parameter_types=self.parameter_types,
            signature=self.signature,
            signature_hash=md5(self.signature),
            visibility="public"
        )


@dataclass
class Codebase:
    functions: List[Function]

    def get_fingerprint(self, config: Config, sort: bool = False):
        m = hashlib.sha256()
        m.update(bytes(str(config.codebase), 'utf-8'))
        m.update(bytes(str(config.packages), 'utf-8'))
        m.update(bytes(str(config.exclude_packages), 'utf-8'))
        m.update(bytes(str(config.exclude_init), 'utf-8'))
        m.update(len(self.functions).to_bytes(1, 'big'))
        functions = sorted(self.functions, key=lambda x: x.name) if sort else self.functions
        for function in functions:
            m.update(bytes(function.signature, 'utf-8'))

        return m.hexdigest()


class SchedulerState:
    name: str
    interval_seconds: int
    retry_interval_seconds: int
    retry_interval_factor: int
    num_failures: int
    next_event_at_seconds: float
    clock: int

    def __init__(self, name):
        self.name = name

    def initialize(self, interval_seconds, retry_interval_seconds):
        self.interval_seconds = interval_seconds
        self.retry_interval_seconds = retry_interval_seconds
        self.next_event_at_seconds = 0
        self.reset_retry_counter()
        return self

    def reset_retry_counter(self):
        self.num_failures = 0
        self.retry_interval_factor = 1

    def update_intervals(self, interval_seconds, retry_interval_seconds):
        if self.next_event_at_seconds != 0:
            if interval_seconds < self.interval_seconds and self.retry_interval_factor == 1:
                self.next_event_at_seconds = time.time() + interval_seconds
            elif retry_interval_seconds < self.retry_interval_seconds and self.retry_interval_factor > 1:
                self.next_event_at_seconds = time.time() + retry_interval_seconds * self.retry_interval_factor

        self.interval_seconds = interval_seconds
        self.retry_interval_seconds = retry_interval_seconds

    def schedule_next(self):
        self.next_event_at_seconds = time.time() + self.interval_seconds
        self.reset_retry_counter()

    def schedule_now(self):
        self.next_event_at_seconds = 0

    def schedule_retry(self):
        back_off_limit = 5

        if self.num_failures < back_off_limit:
            self.retry_interval_factor = 1
        else:
            self.retry_interval_factor = int(math.pow(2, min(self.num_failures - back_off_limit + 1, 4)))

        self.next_event_at_seconds = time.time() + self.retry_interval_seconds * self.retry_interval_factor
        self.num_failures += 1

    def is_due_time(self):
        return time.time() >= self.next_event_at_seconds
