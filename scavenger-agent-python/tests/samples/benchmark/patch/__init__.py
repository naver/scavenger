import uuid


def sum_10_thousand():
    sum_ = 0
    for i in range(0, 10000):
        sum_ += i
    return sum_


def count():
    return len(str(uuid.uuid4()))
