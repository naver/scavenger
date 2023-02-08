import logging
import unittest
from pathlib import Path

from scavenger.config import Config
from scavenger.internal.model import Function, Codebase
from scavenger.internal.scan import CodeBaseScanner, PyFile

logging.basicConfig(level=logging.DEBUG)


class TestScanSample1(unittest.TestCase):

    def setUp(self):
        self.sample_absolute_path = Path(__file__).parent.joinpath("sample")
        self.codebase_scanner = CodeBaseScanner([self.sample_absolute_path], ["views"], [], True)

    def test_scan(self):
        expected_functions = [
            Function(
                name="dispatch_request",
                declaring_type="views.user.UserList",
                signature="views.user.UserList.dispatch_request(self)",
                parameter_types="self",
                package_name="views.user",
            ),
            Function(
                name="dispatch_request",
                declaring_type="views.user.UserDetail",
                signature="views.user.UserDetail.dispatch_request(self, id)",
                parameter_types="self, id",
                package_name="views.user",
            ),
            Function(
                name="test",
                declaring_type="views.user.UserList.NestedClass",
                signature="views.user.UserList.NestedClass.test(self)",
                parameter_types="self",
                package_name="views.user",
            ),
            Function(
                name="dead_function",
                declaring_type="views.dead",
                signature="views.dead.dead_function()",
                parameter_types="",
                package_name="views.dead",
            ),
            Function(
                name="live_function",
                declaring_type="views.live",
                signature="views.live.live_function(arg1, *args, **kwargs)",
                parameter_types="arg1, *args, **kwargs",
                package_name="views.live",
            ),
        ]
        expected = Codebase(
            functions=expected_functions
        )
        config = Config(
            server_url="",
            api_key="",
            app_name="",
            environment="",
            codebase=[],
            packages=[]
        )

        self.assertEqual(self.codebase_scanner.scan().get_fingerprint(config, True), expected.get_fingerprint(config, True))

    def test_find_all_py_files(self):
        expected = [PyFile(codebase_path=self.sample_absolute_path, relative_path=Path("views").joinpath("user.py")),
                    PyFile(codebase_path=self.sample_absolute_path, relative_path=Path("views").joinpath("dead.py")),
                    PyFile(codebase_path=self.sample_absolute_path,
                           relative_path=Path("views").joinpath("__init__.py")),
                    PyFile(codebase_path=self.sample_absolute_path, relative_path=Path("views").joinpath("live.py"))]

        self.assertListEqual(self.codebase_scanner.find_all_py_files(), expected)


class TestScanSample3(unittest.TestCase):

    def setUp(self):
        self.sample_absolute_path = Path(__file__).parent.joinpath("sample3")
        self.codebase_scanner = CodeBaseScanner([self.sample_absolute_path], ["polls", "mysite"], ["polls.migrations"], True)

    def test_scan(self):
        codebase = self.codebase_scanner.scan()

        expected = ["polls.models.Question.__str__", "polls.models.Question.was_published_recently",
                    "polls.models.Choice.__str__", "polls.tests.create_question",
                    "polls.tests.QuestionModelTests.test_was_published_recently_with_future_question",
                    "polls.tests.QuestionModelTests.test_was_published_recently_with_old_question",
                    "polls.tests.QuestionModelTests.test_was_published_recently_with_recent_question",
                    "polls.tests.QuestionIndexViewTests.test_no_questions",
                    "polls.tests.QuestionIndexViewTests.test_past_question",
                    "polls.tests.QuestionIndexViewTests.test_future_question",
                    "polls.tests.QuestionIndexViewTests.test_future_question_and_past_question",
                    "polls.tests.QuestionIndexViewTests.test_two_past_questions",
                    "polls.tests.QuestionDetailViewTests.test_future_question",
                    "polls.tests.QuestionDetailViewTests.test_past_question", "polls.views.vote",
                    "polls.views.IndexView.get_queryset", "polls.views.DetailView.get_queryset"]
        self.assertEqual(len(codebase.functions), len(expected))

    def test_find_all_py_files(self):
        expected = [PyFile(codebase_path=Path(self.sample_absolute_path), relative_path=Path("polls/models.py")),
                    PyFile(codebase_path=Path(self.sample_absolute_path), relative_path=Path("polls/__init__.py")),
                    PyFile(codebase_path=Path(self.sample_absolute_path), relative_path=Path("polls/apps.py")),
                    PyFile(codebase_path=Path(self.sample_absolute_path), relative_path=Path("polls/admin.py")),
                    PyFile(codebase_path=Path(self.sample_absolute_path), relative_path=Path("polls/tests.py")),
                    PyFile(codebase_path=Path(self.sample_absolute_path), relative_path=Path("polls/urls.py")),
                    PyFile(codebase_path=Path(self.sample_absolute_path), relative_path=Path("polls/views.py")),
                    PyFile(codebase_path=Path(self.sample_absolute_path), relative_path=Path("mysite/__init__.py")),
                    PyFile(codebase_path=Path(self.sample_absolute_path), relative_path=Path("mysite/settings.py")),
                    PyFile(codebase_path=Path(self.sample_absolute_path), relative_path=Path("mysite/urls.py")),
                    PyFile(codebase_path=Path(self.sample_absolute_path), relative_path=Path("mysite/wsgi.py"))]

        self.assertListEqual(self.codebase_scanner.find_all_py_files(), expected)
