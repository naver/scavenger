def deco(func):
    return func


def deco2():
    def wrapper(func):
        return func

    return wrapper
