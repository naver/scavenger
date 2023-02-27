import hashlib
import time
from typing import List


def current_milli_time():
    return round(time.time() * 1000)


def md5(str_):
    return hashlib.md5(bytes(str_, "utf-8")).hexdigest()


def remove_prefix(str_: str, prefix: str):
    if str_.startswith(prefix):
        return str_[len(prefix):]
    return str_


def remove_suffix(str_: str, suffix: str):
    if str_.endswith(suffix):
        return str_[0:len(str_) - len(suffix)]
    return str_


def filter_by_exclude_packages(target: str, exclude_packages: List[str]):
    for exclude_package in exclude_packages:
        if target.startswith(exclude_package):
            return True
    return False
