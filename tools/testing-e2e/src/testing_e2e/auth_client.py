from __future__ import annotations

from dataclasses import dataclass

from testing_e2e.transport import ActionResult, ActionTransport


@dataclass(frozen=True)
class AuthClient:
    """Facade for the auth action group used by the e2e tests."""

    transport: ActionTransport

    def admin_bootstrap(self, username: str, fullname: str, password: str, secret: str) -> ActionResult:
        return self.transport.invoke(
            "auth",
            "admin_bootstrap",
            {
                "username": username,
                "fullname": fullname,
                "password": password,
                "secret": secret,
            },
            None,
        )

    def login(self, username: str, password: str) -> ActionResult:
        return self.transport.invoke(
            "auth",
            "login",
            {
                "username": username,
                "password": password,
            },
            None,
        )

    def whoami(self, access_token: str) -> ActionResult:
        return self.transport.invoke("auth", "whoami", {}, access_token)
