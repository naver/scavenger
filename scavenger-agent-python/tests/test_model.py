import unittest

from scavenger.config import Config
from scavenger.internal.model import Codebase, Function


class TestModel(unittest.TestCase):
    def test_calculate_fingerprint(self):
        codebase = Codebase(
            functions=[Function(
                name="live_function",
                declaring_type="views.live",
                signature="views.live.live_function(arg1)",
                parameter_types="arg1",
                package_name='views.live'
            )]
        )
        config = Config(
            server_url="",
            api_key="",
            app_name="",
            environment="",
            codebase=["a"],
            packages=["b"]
        )
        fingerprint = codebase.get_fingerprint(config)
        self.assertEqual(codebase.get_fingerprint(config), fingerprint)
        config.server_url = "1"
        self.assertEqual(codebase.get_fingerprint(config), fingerprint)
        config.packages = ["c"]
        self.assertNotEqual(codebase.get_fingerprint(config), fingerprint)
