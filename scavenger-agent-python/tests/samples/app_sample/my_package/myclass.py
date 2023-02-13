class MyClass:
    def __init__(self):
        pass

    def foo(self):
        pass

    @staticmethod
    def static_method(arg):
        return arg

    @classmethod
    def class_method(cls):
        return cls

    class NestedClass:
        def baz(self, arg1, *args, **kwargs):
            pass

        @staticmethod
        def bazz():
            pass
