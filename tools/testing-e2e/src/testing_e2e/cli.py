from __future__ import annotations

import json
import os
import subprocess
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from testcontainers.core.container import DockerContainer

from testing_e2e.transport import ActionTransport


@dataclass(frozen=True)
class CommandResult:
    exit_code: int
    stdout: str
    stderr: str

    @property
    def status_code(self) -> int:
        return self.exit_code

    def json(self) -> Any:
        return json.loads(self.stdout)


@dataclass(frozen=True)
class MedatarunLocalCliTransport(ActionTransport):
    """Run the CLI from the test environment against the Docker-hosted server."""

    executable: Path
    base_url: str

    def invoke(
        self,
        action_group_key: str,
        action_key: str,
        parameters: dict[str, Any],
        access_token: str | None,
    ) -> CommandResult:
        args = _build_cli_args(action_group_key, action_key, parameters)
        env = os.environ.copy()
        env["MEDATARUN_PUBLIC_BASE_URL"] = self.base_url
        if access_token is not None:
            env["MEDATARUN_AUTH_TOKEN"] = access_token
        completed = subprocess.run(
            [str(self.executable), *args],
            capture_output=True,
            text=True,
            env=env,
            check=False,
        )
        return CommandResult(completed.returncode, completed.stdout, completed.stderr)


@dataclass(frozen=True)
class MedatarunDockerCliTransport(ActionTransport):
    """Run the CLI inside the same Docker container as the application."""

    executable: Path
    container: DockerContainer

    def invoke(
        self,
        action_group_key: str,
        action_key: str,
        parameters: dict[str, Any],
        access_token: str | None,
    ) -> CommandResult:
        args = _build_cli_args(action_group_key, action_key, parameters)
        environment: dict[str, str] = {}
        if access_token is not None:
            environment["MEDATARUN_AUTH_TOKEN"] = access_token
        result = self.container.get_wrapped_container().exec_run(
            [str(self.executable), *args],
            environment=environment or None,
            demux=True,
        )
        stdout_bytes, stderr_bytes = _split_demux_output(result.output)
        return CommandResult(
            result.exit_code,
            stdout_bytes.decode("utf-8", errors="replace"),
            stderr_bytes.decode("utf-8", errors="replace"),
        )


def _build_cli_args(action_group_key: str, action_key: str, parameters: dict[str, Any]) -> list[str]:
    args = [action_group_key, action_key]
    for key, value in parameters.items():
        args.append(f"--{key}={_format_parameter_value(value)}")
    return args


def _format_parameter_value(value: Any) -> str:
    if isinstance(value, str):
        return value
    if isinstance(value, (int, float, bool)) or value is None:
        return json.dumps(value)
    return json.dumps(value)


def _split_demux_output(output: object) -> tuple[bytes, bytes]:
    if output is None:
        return b"", b""
    if isinstance(output, tuple):
        stdout = output[0] or b""
        stderr = output[1] or b""
        return stdout, stderr
    if isinstance(output, bytes):
        return output, b""
    return str(output).encode("utf-8"), b""
