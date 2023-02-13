import logging
import unittest
from time import sleep

from scavenger.internal.invocation_registry import InvocationRegistry
from scavenger.internal.patch import Finder, Patcher
from scavenger.internal.util import md5

logging.basicConfig(level=logging.DEBUG)


class TestPatcher(unittest.TestCase):
    def test_patch(self):
        invocation_registry = InvocationRegistry()
        Patcher(["samples.app_sample"], ["samples.app_sample.exclude_packages"], [], True, invocation_registry).patch()
        try:
            from samples.app_sample.my_package.myclass import MyClass
            from samples.app_sample.my_package import mymodule, mymodule2
            from samples.app_sample.exclude_packages import exclude_function
        except ModuleNotFoundError:
            self.skipTest("# 경로 문제로 파일 단독 테스트만 가능")
            return
        self.assertEqual(MyClass.class_method(), MyClass)
        self.assertEqual(MyClass().class_method(), MyClass)
        self.assertEqual(MyClass.static_method(1), 1)
        self.assertEqual(MyClass().static_method(1), 1)
        MyClass().foo()
        MyClass.NestedClass().baz(1)
        MyClass.NestedClass.bazz()
        mymodule.baz()
        mymodule2.bazz()
        exclude_function()
        sleep(1)
        expected = {md5(signature) for signature in {'samples.app_sample.my_package.myclass.MyClass.static_method(arg)',
                                                     'samples.app_sample.my_package.myclass.MyClass.class_method(cls)',
                                                     'samples.app_sample.my_package.myclass.MyClass.foo(self)',
                                                     'samples.app_sample.my_package.myclass.MyClass.NestedClass.baz(self, arg1, '
                                                     '*args, **kwargs)',
                                                     'samples.app_sample.my_package.myclass.MyClass.NestedClass.bazz()',
                                                     'samples.app_sample.my_package.mymodule.baz()',
                                                     'samples.app_sample.my_package.mymodule2.bazz()'}}
        self.assertSetEqual(set(invocation_registry.get_invocations()[0]), expected)

    def test_patch_decorated(self):
        invocation_registry = InvocationRegistry()
        Patcher(["samples.app_sample"], ["samples.app_sample.exclude_packages"], ["@deco", "@deco2"], True, invocation_registry).patch()
        try:
            from samples.app_sample.decorator import decorator
        except ModuleNotFoundError:
            self.skipTest("# 경로 문제로 파일 단독 테스트만 가능")
            return

        decorator.deco2()
        decorator.decorated_function()
        decorator.decorated_function2()
        decorator.not_decorated_function()
        decorator.A().a()
        decorator.A.b()
        decorator.A.c()

        sleep(1)
        expected = {md5(signature) for signature in {'samples.app_sample.decorator.decorator.decorated_function()',
                                                     'samples.app_sample.decorator.decorator.decorated_function2()',
                                                     'samples.app_sample.decorator.decorator.decorated_function2()',
                                                     'samples.app_sample.decorator.decorator.A.a(self)',
                                                     'samples.app_sample.decorator.decorator.A.b()',
                                                     'samples.app_sample.decorator.decorator.A.c(cls_)'}}
        self.assertSetEqual(set(invocation_registry.get_invocations()[0]), expected)


class TestFinder(unittest.TestCase):
    def test_patch_required(self):
        invocation_registry = InvocationRegistry()
        finder = Finder(["views"], [], [], False, invocation_registry)
        self.assertTrue(finder.patch_required("views"))
        self.assertTrue(finder.patch_required("views.user"))
        self.assertTrue(finder.patch_required("views.user.abc"))

    def test_patch_unnecessary(self):
        invocation_registry = InvocationRegistry()
        finder = Finder(["views"], [], [], False, invocation_registry)
        self.assertFalse(finder.patch_required("view.user"))
        self.assertFalse(finder.patch_required("abc.views"))
