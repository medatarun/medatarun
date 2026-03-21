from __future__ import annotations

import re
import time
from dataclasses import dataclass
from pathlib import Path

from testcontainers.core.container import DockerContainer


BOOTSTRAP_SECRET_PATTERN = re.compile(r"BOOTSTRAP SECRET \(one-time usage\): (?P<secret>.+)")


@dataclass(frozen=True)
class RunningApplication:
    container: DockerContainer
    base_url: str
    bootstrap_secret: str

    def stop(self) -> None:
        self.container.stop()

    def logs_text(self) -> str:
        stdout, stderr = self.container.get_logs()
        return stdout.decode("utf-8", errors="replace") + stderr.decode("utf-8", errors="replace")


def start_application(
    medatarun_home: Path,
    bootstrap_secret: str | None,
) -> RunningApplication:
    container = DockerContainer("eclipse-temurin:21-jre")
    container.with_volume_mapping(str(medatarun_home), "/opt/medatarun", mode="rw")
    if bootstrap_secret is not None:
        container.with_env("MEDATARUN_AUTH_BOOTSTRAP_SECRET", bootstrap_secret)
    container.with_exposed_ports(8080)
    container.with_command(["/opt/medatarun/medatarun", "serve"])
    container.start()
    wait_for_startup(container)

    logs_text = read_logs(container)
    actual_bootstrap_secret = extract_bootstrap_secret(logs_text)
    if actual_bootstrap_secret is None:
        raise RuntimeError("The bootstrap secret was not found in the application logs")
    if bootstrap_secret is not None and actual_bootstrap_secret != bootstrap_secret:
        raise RuntimeError("The predefined bootstrap secret was not the one reported in the logs")

    host = container.get_container_host_ip()
    port = container.get_exposed_port(8080)
    return RunningApplication(container=container, base_url=f"http://{host}:{port}", bootstrap_secret=actual_bootstrap_secret)


def wait_for_startup(container: DockerContainer) -> None:
    marker = "Starting REST API on http://0.0.0.0:8080 with publicBaseUrl="
    deadline = time.monotonic() + 120
    while time.monotonic() < deadline:
        if marker in read_logs(container):
            return
        time.sleep(0.5)
    raise RuntimeError("Medatarun did not start within 120 seconds")


def extract_bootstrap_secret(logs_text: str) -> str | None:
    match = BOOTSTRAP_SECRET_PATTERN.search(logs_text)
    if match is None:
        return None
    return match.group("secret").strip()


def read_logs(container: DockerContainer) -> str:
    stdout, stderr = container.get_logs()
    return stdout.decode("utf-8", errors="replace") + stderr.decode("utf-8", errors="replace")
