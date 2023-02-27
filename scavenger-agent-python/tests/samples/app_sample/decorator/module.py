from a import decorator
from decorator import deco


@decorator.deco
def foo():
    pass


@deco
def bar():
    pass


@decorator.deco2()
def baz():
    pass
