def deco(func):
    return func


def deco2():
    def wrapper(func):
        return func

    return wrapper


@deco
def decorated_function():
    pass


@deco2()
def decorated_function2():
    pass


def not_decorated_function():
    pass


class A:
    @deco
    def a(self):
        pass

    @staticmethod
    @deco
    def b():
        pass

    @classmethod
    @deco
    def c(cls_):
        pass
