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
        Patcher(["sample2"], ["sample2.exclude_packages"], invocation_registry).patch()
        try:
            from sample2.my_package.myclass import MyClass
            from sample2.my_package import mymodule, mymodule2
            from sample2.exclude_packages import exclude_function
        except ModuleNotFoundError:
            invocation_registry.stop_thread_event.set()
            self.skipTest("# 경로 문제로 파일 단독 테스트만 가능")
            return

        MyClass.bar()
        MyClass().foo()
        MyClass.NestedClass().baz(1)
        MyClass.NestedClass.bazz()
        mymodule.baz()
        mymodule2.bazz()
        exclude_function()
        sleep(1)
        expected = {md5(signature) for signature in {'sample2.my_package.myclass.MyClass.bar()',
                                                     'sample2.my_package.myclass.MyClass.__init__(self)',
                                                     'sample2.my_package.myclass.MyClass.foo(self)',
                                                     'sample2.my_package.myclass.MyClass.NestedClass.baz(self, arg1, '
                                                     '*args, **kwargs)',
                                                     'sample2.my_package.myclass.MyClass.NestedClass.bazz()',
                                                     'sample2.my_package.mymodule.baz()',
                                                     'sample2.my_package.mymodule2.bazz()'}}
        self.assertSetEqual(set(invocation_registry.get_invocations()[0]), expected)
        invocation_registry.stop_thread_event.set()


class TestFinder(unittest.TestCase):
    def test_patch_required(self):
        invocation_registry = InvocationRegistry()
        finder = Finder(["views"], [], invocation_registry)
        self.assertTrue(finder.patch_required("views"))
        self.assertTrue(finder.patch_required("views.user"))
        self.assertTrue(finder.patch_required("views.user.abc"))
        invocation_registry.stop_thread_event.set()

    def test_patch_unnecessary(self):
        invocation_registry = InvocationRegistry()
        finder = Finder(["views"], [], invocation_registry)
        self.assertFalse(finder.patch_required("view.user"))
        self.assertFalse(finder.patch_required("abc.views"))
        invocation_registry.stop_thread_event.set()
