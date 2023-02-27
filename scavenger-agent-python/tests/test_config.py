import pathlib
import unittest

from scavenger.config import Config


class TestBanner(unittest.TestCase):
    def test_load_config(self):
        config = Config.load_config(str(pathlib.Path(__file__).parent.joinpath("scavenger.conf")))
        self.assertEqual(config.api_key, "test_api_key")
        self.assertEqual(config.server_url, "test_server_url2")
        self.assertEqual(config.codebase, ["ax"])
        self.assertEqual(config.decorators, ["@abc"])
        self.assertEqual(config.packages, ["ab", "cd"])
        self.assertEqual(config.scheduler_initial_delay_seconds, 1)
        self.assertEqual(config.async_codebase_scan_mode, True)
        self.assertEqual(config.debug_mode, False)
        self.assertEqual(config.exclude_init, True)

    def test_load_config_not_found(self):
        self.assertRaises(FileNotFoundError, lambda: Config.load_config("ahtckwrpTwl"))
