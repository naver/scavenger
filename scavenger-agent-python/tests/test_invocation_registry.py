import unittest
from time import sleep

from scavenger.internal.invocation_registry import InvocationRegistry


class TestInvocationRegistry(unittest.TestCase):
    def test_invocation_registry(self):
        invocation_registry = InvocationRegistry()
        invocation_registry.register("a")
        invocation_registry.register("b")
        sleep(0.5)
        self.assertSetEqual(set(invocation_registry.get_invocations()[0]), {"a", "b"})
        self.assertEqual(invocation_registry.current_index, invocation_registry.BACK_BUFFER_INDEX)
        invocation_registry.register("b")
        sleep(0.5)
        self.assertSetEqual(set(invocation_registry.get_invocations()[0]), {"b"})
