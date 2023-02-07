from __future__ import annotations

import time
from _queue import Empty
from queue import Queue
from threading import Thread
from typing import List, Set

from scavenger.internal.util import current_milli_time


class InvocationRegistry:
    queue: Queue[str]
    invocations: List[Set[str]]
    recording_interval_started_at_millis: int
    current_index: int
    FRONT_BUFFER_INDEX = 0
    BACK_BUFFER_INDEX = 1

    def __init__(self):
        self.invocations = [set(), set()]
        self.queue = Queue()
        self.current_index = self.FRONT_BUFFER_INDEX
        self.recording_interval_started_at_millis = current_milli_time()
        self.thread = Thread(target=self._update_invocations, daemon=True)
        self.thread.start()

    def register(self, hash_: str):
        self.queue.put(hash_)

    def get_invocations(self):
        time.sleep(0.01)
        old_recording_interval_started_at_millis = self.recording_interval_started_at_millis
        old_index = self.current_index
        self._toggle_index()
        invocations = list(self.invocations[old_index])
        self.invocations[old_index].clear()
        return invocations, old_recording_interval_started_at_millis

    def empty(self):
        return self.queue.empty() and len(self.invocations[self.FRONT_BUFFER_INDEX]) == 0 and len(
            self.invocations[self.BACK_BUFFER_INDEX]) == 0

    def _update_invocations(self):
        while True:
            try:
                hash_ = self.queue.get()
            except Empty:
                continue
            self.invocations[self.current_index].add(hash_)

    def _toggle_index(self):
        self.recording_interval_started_at_millis = current_milli_time()
        self.current_index = self.FRONT_BUFFER_INDEX \
            if self.current_index == self.BACK_BUFFER_INDEX else self.BACK_BUFFER_INDEX
