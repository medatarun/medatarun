from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any, Protocol


class ActionResult(Protocol):
    @property
    def exit_code(self) -> int:
        pass

    def json(self) -> Any:
        pass


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
