import socket
import uuid

from scavenger.internal.util import current_milli_time

PROCESS_START_AT_MILLIS = current_milli_time()
PROCESS_UUID = str(uuid.uuid4())
HOSTNAME = socket.gethostname()
