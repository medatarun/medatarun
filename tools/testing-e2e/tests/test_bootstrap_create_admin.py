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

# Create admin
#
# - Start the application (depending on the secret variant)
# - create an admin with (depending on the client variant)
#   `medatarun auth admin_bootstrap --username=admin --fullname="Administrator" --password="..." --secret=...`
# - request an access token (depending on the client variant)
#   `medatarun auth login --username=admin --password="..."` which returns a
#   JSON object with `.access_token`
# - check with the client (depending on the client variant)
#   `medatarun auth whoami` that the result is a JSON object with `sub == admin`
#   and `admin == true` as a boolean in the output

@pytest.mark.parametrize("scenario", ADMIN_BOOTSTRAP_SCENARIOS)
def test_bootstrap_create_admin(run_config: RunConfig, scenario: AdminBootstrapScenario) -> None:
    expected_success_code = 200 if scenario.client == ClientVariant.API else 0
    with TestEnvironment(run_config, scenario.client, scenario.secret) as env:
        client = env.client()

        username = "admin"
        fullname = "Administrator"
        password = secrets.token_urlsafe(24)
        secret = env.application.bootstrap_secret

        bootstrap_result = client.admin_bootstrap(username, fullname, password, secret)
        assert bootstrap_result.exit_code == expected_success_code

        login_result = client.login(username, password)
        assert login_result.exit_code == expected_success_code
        access_token = login_result.json()["access_token"]

        whoami_result = client.whoami(access_token)
        assert whoami_result.exit_code == expected_success_code
        whoami = whoami_result.json()
        assert whoami["sub"] == username
        assert whoami["admin"] is True
