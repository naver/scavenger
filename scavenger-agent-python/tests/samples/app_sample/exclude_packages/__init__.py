from tests.samples.app_sample.decorator.decorator import deco


def exclude_function():
    pass


@deco
def decorated_exclude_function():
    pass
