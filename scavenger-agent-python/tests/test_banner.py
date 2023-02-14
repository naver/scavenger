import unittest

from scavenger.config import Config
from scavenger.internal.banner import Banner


class TestBanner(unittest.TestCase):
    def test_banner(self):
        Banner(Config(
            server_url="test_server_url",
            api_key="test_api_key",
            codebase=["path1", "path2"],
            packages=["package1", "package2"],
        )).print()
