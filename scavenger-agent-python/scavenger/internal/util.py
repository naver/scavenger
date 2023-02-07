import hashlib
import time


def current_milli_time():
    return round(time.time() * 1000)


def md5(str_):
    return hashlib.md5(bytes(str_, "utf-8")).hexdigest()
