from flask.views import View

from views import live


class UserList(View):
    class NestedClass:
        def test(self):
            pass

    def dispatch_request(self):
        live.live_function("hello")
        return "<p>Hello, Users!</p>"


class UserDetail(View):
    def dispatch_request(self, id):
        return f'<p>Hello, User : {id}</p>'
