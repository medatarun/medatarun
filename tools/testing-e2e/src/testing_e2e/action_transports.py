from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

from testcontainers.core.container import DockerContainer

from testing_e2e.api import MedatarunApiTransport
from testing_e2e.cli import MedatarunDockerCliTransport, MedatarunLocalCliTransport
from testing_e2e.transport import ActionTransport


@dataclass(frozen=True)
class ActionTransports:
    """Build the action transports used by the e2e tests from injected runtime dependencies."""

    base_url: str
    cli_local_executable: Path | None
    cli_container_executable: Path
    docker_container: DockerContainer

    def api(self) -> ActionTransport:
        return MedatarunApiTransport(self.base_url)

    def cli_local(self) -> ActionTransport:
        if self.cli_local_executable is None:
            raise RuntimeError("CLI local executable is missing for the CLI test environment transport")
        return MedatarunLocalCliTransport(self.cli_local_executable, base_url=self.base_url)

    def cli_container(self) -> ActionTransport:
        return MedatarunDockerCliTransport(self.cli_container_executable, container=self.docker_container)
