import uuid
import socket

from scavenger.internal.util import current_milli_time

JVM_START_AT_MILLIS = current_milli_time()
JVM_UUID = str(uuid.uuid4())
HOSTNAME = socket.gethostname()
