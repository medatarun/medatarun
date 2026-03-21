from __future__ import annotations

import secrets

import pytest

from testing_e2e.run_config import RunConfig
from testing_e2e.scenario import AdminBootstrapScenario, ClientVariant, SecretVariant
from testing_e2e.test_environment import TestEnvironment


ADMIN_BOOTSTRAP_SCENARIOS = [
    pytest.param(
        AdminBootstrapScenario(ClientVariant.API, SecretVariant.LOGS),
        id="api-logs",
    ),
    pytest.param(
        AdminBootstrapScenario(ClientVariant.API, SecretVariant.PREDEFINED),
        id="api-predefined",
    ),
    pytest.param(
        AdminBootstrapScenario(ClientVariant.CLI_CONTAINER, SecretVariant.LOGS),
        id="cli-container-logs",
    ),
    pytest.param(
        AdminBootstrapScenario(ClientVariant.CLI_CONTAINER, SecretVariant.PREDEFINED),
        id="cli-container-predefined",
    ),
    pytest.param(
        AdminBootstrapScenario(ClientVariant.CLI_TEST_ENV, SecretVariant.LOGS),
        id="cli-test-env-logs",
    ),
    pytest.param(
        AdminBootstrapScenario(ClientVariant.CLI_TEST_ENV, SecretVariant.PREDEFINED),
        id="cli-test-env-predefined",
    ),
]


@pytest.mark.parametrize("scenario", ADMIN_BOOTSTRAP_SCENARIOS)
def test_admin_bootstrap(run_config: RunConfig, scenario: AdminBootstrapScenario) -> None:
    with TestEnvironment(run_config, scenario.client, scenario.secret) as env:
        client = env.client()

        username = "admin"
        fullname = "Administrator"
        password = secrets.token_urlsafe(24)
        secret = env.application.bootstrap_secret

        bootstrap_result = client.admin_bootstrap(username, fullname, password, secret)
        assert bootstrap_result.exit_code == 0

        login_result = client.login(username, password)
        assert login_result.exit_code == 0
        access_token = login_result.json()["access_token"]

        whoami_result = client.whoami(access_token)
        assert whoami_result.exit_code == 0
        whoami = whoami_result.json()
        assert whoami["sub"] == username
        assert whoami["admin"] is True
