from __future__ import annotations

import json
import urllib.error
import urllib.request
from dataclasses import dataclass
from typing import Any

from testing_e2e.transport import ActionResult, ActionTransport

@dataclass(frozen=True)
class HttpResult(ActionResult):
    _status_code: int
    _body: str

    @property
    def stderr(self) -> str:
        raise NotImplementedError("HTTP results do not expose stderr")

    def json(self) -> Any:
        return json.loads(self._body)

    def is_status_code(self, status_code: int) -> bool:
        return self._status_code == status_code


@dataclass(frozen=True)
class MedatarunApiTransport(ActionTransport):
    base_url: str

    def invoke(
        self,
        action_group_key: str,
        action_key: str,
        parameters: dict[str, Any],
        access_token: str | None,
    ) -> HttpResult:
        """Invoke one API action using the same action-group/action-key split as the server."""
        headers = {"Content-Type": "application/json"}
        if access_token is not None:
            headers["Authorization"] = f"Bearer {access_token}"
        request = urllib.request.Request(
            self._url(f"/api/{action_group_key}/{action_key}"),
            data=json.dumps(parameters).encode("utf-8"),
            method="POST",
            headers=headers,
        )
        return self._request(request)

    def _request(self, request: urllib.request.Request) -> HttpResult:
        try:
            with urllib.request.urlopen(request) as response:
                return HttpResult(response.status, response.read().decode("utf-8"))
        except urllib.error.HTTPError as error:
            return HttpResult(error.code, error.read().decode("utf-8"))

    def _url(self, path: str) -> str:
        return f"{self.base_url}{path}"
