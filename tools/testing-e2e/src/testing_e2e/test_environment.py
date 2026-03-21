from __future__ import annotations

import secrets
import tempfile
from enum import Enum
from pathlib import Path

from testing_e2e.action_transports import ActionTransports
from testing_e2e.application import RunningApplication, start_application
from testing_e2e.auth_client import AuthClient
from testing_e2e.distribution_resolver import DistributionResolver
from testing_e2e.distribution_unpacker import DistributionUnpacker
from testing_e2e.run_config import RunConfig


class ClientVariant(Enum):
    API = "api"
    CLI_CONTAINER = "cli_container"
    CLI_TEST_ENV = "cli_test_env"


class SecretVariant(Enum):
    LOGS = "logs"
    PREDEFINED = "predefined"


class TestEnvironment:
    """Own the full e2e runtime for one scenario.

    The constructor resolves the distribution, unpacks the runtime, starts the
    application, and builds the action transports. In the predefined-secret
    case, it generates the bootstrap secret locally and passes it to the
    Docker container at startup. Use ``close()`` or a ``with`` block to stop
    the application and release the workspace.
    """

    __test__ = False

    def __init__(
        self,
        run_config: RunConfig,
        client_variant: ClientVariant,
        secret_variant: SecretVariant,
    ) -> None:
        self.client_variant = client_variant
        self._workspace = tempfile.TemporaryDirectory(prefix="testing-e2e-")

        distribution_zip = DistributionResolver(run_config).resolve()
        workspace = Path(self._workspace.name)
        server_home = DistributionUnpacker(distribution_zip, workspace / "server").unpack()

        cli_local_executable = None
        if client_variant == ClientVariant.CLI_TEST_ENV:
            cli_home = DistributionUnpacker(distribution_zip, workspace / "cli").unpack()
            cli_local_executable = cli_home / "medatarun"

        bootstrap_secret = None
        if secret_variant == SecretVariant.PREDEFINED:
            # This secret is generated for the test run and injected into the Docker startup.
            bootstrap_secret = secrets.token_urlsafe(32)

        self.application: RunningApplication = start_application(server_home, bootstrap_secret)
        self.transports: ActionTransports = ActionTransports(
            base_url=self.application.base_url,
            cli_local_executable=cli_local_executable,
            cli_container_executable=Path("/opt/medatarun/medatarun"),
            docker_container=self.application.container,
        )

    def close(self) -> None:
        """Stop the application and clean up the temporary workspace."""
        self.application.stop()
        self._workspace.cleanup()

    def client(self) -> AuthClient:
        """Build the auth client for the variant selected for this environment."""
        if self.client_variant == ClientVariant.API:
            transport = self.transports.api()
        elif self.client_variant == ClientVariant.CLI_CONTAINER:
            transport = self.transports.cli_container()
        elif self.client_variant == ClientVariant.CLI_TEST_ENV:
            transport = self.transports.cli_local()
        else:
            raise RuntimeError(f"Unsupported client variant: {self.client_variant}")
        return AuthClient(transport)

    def __enter__(self) -> TestEnvironment:
        return self

    def __exit__(self, exc_type: object, exc_value: object, traceback: object) -> bool:
        self.close()
        return False
