from __future__ import annotations

from dataclasses import dataclass

from testing_e2e.test_environment import ClientVariant, SecretVariant


@dataclass(frozen=True)
class AdminBootstrapScenario:
    client: ClientVariant
    secret: SecretVariant

    def __str__(self) -> str:
        return f"{self.client.value}:{self.secret.value}"
