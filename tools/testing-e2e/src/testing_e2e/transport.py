from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any


class ActionResult(ABC):
    """Result returned by one application action.

    The same contract is used for HTTP and CLI transports so tests can read the
    payload and the transport-specific code without guessing the concrete type.
    An accessor that does not make sense for a transport must raise in the
    concrete implementation.
    `is_status_code()` compares a transport result to an HTTP status code:
    HTTP implementations compare directly, CLI implementations map success to
    `200` and failure to any other status code.
    """

    @abstractmethod
    def json(self) -> Any:
        ...

    @abstractmethod
    def is_status_code(self, status_code: int) -> bool:
        ...

    @abstractmethod
    def has_error_text(self, message: str) -> bool:
        ...


class ActionTransport(ABC):
    """Execute one application action identified by group key and action key."""

    @abstractmethod
    def invoke(
        self,
        action_group_key: str,
        action_key: str,
        parameters: dict[str, Any],
        access_token: str | None,
    ) -> ActionResult:
        raise NotImplementedError
