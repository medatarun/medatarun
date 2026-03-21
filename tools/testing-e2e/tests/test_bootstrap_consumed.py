from __future__ import annotations

import secrets

import pytest

from testing_e2e.run_config import RunConfig
from testing_e2e.scenario import ClientVariant, SecretVariant
from testing_e2e.test_environment import TestEnvironment

BOOTSTRAP_CONSUMED_CLIENTS = [
    pytest.param(ClientVariant.API, id="api"),
    pytest.param(ClientVariant.CLI_CONTAINER, id="cli-container"),
    pytest.param(ClientVariant.CLI_TEST_ENV, id="cli-test-env"),
]

# Boostrap consumed
#
# - Start the application with a predefined secret
# - create an admin user (depending on the client variant) with `medatarun auth
#   admin_bootstrap`, which must succeed
# - create an admin user again with `medatarun auth admin_bootstrap`
#   (depending on the client) and a new password, which must fail with
#   `title = Bootstrap already consumed` in the returned JSON
# - try to log in with `admin` and the first password (depending on the client)
# - verify with `medatarun auth whoami` (depending on the client) that the right
#   user is returned and that it is an admin in the JSON result

@pytest.mark.parametrize("client_variant", BOOTSTRAP_CONSUMED_CLIENTS)
def test_bootstrap_consumed(run_config: RunConfig, client_variant: ClientVariant) -> None:
    expected_success_code = 200 if client_variant == ClientVariant.API else 0
    expected_failure_code = 410 if client_variant == ClientVariant.API else 1

    with TestEnvironment(run_config, client_variant, SecretVariant.PREDEFINED) as env:
        client = env.client()

        username = "admin"
        fullname = "Administrator"
        first_password = secrets.token_urlsafe(24)
        second_password = secrets.token_urlsafe(24)
        secret = env.application.bootstrap_secret

        first_bootstrap = client.admin_bootstrap(username, fullname, first_password, secret)
        assert first_bootstrap.exit_code == expected_success_code

        second_bootstrap = client.admin_bootstrap(username, fullname, second_password, secret)
        assert second_bootstrap.exit_code == expected_failure_code
        if client_variant == ClientVariant.API:
            assert second_bootstrap.json()["details"] == "Bootstrap already consumed."
        else:
            assert "Bootstrap already consumed." in second_bootstrap.stderr

        login_result = client.login(username, first_password)
        assert login_result.exit_code == expected_success_code
        access_token = login_result.json()["access_token"]

        whoami_result = client.whoami(access_token)
        assert whoami_result.exit_code == expected_success_code
        whoami = whoami_result.json()
        assert whoami["sub"] == username
        assert whoami["admin"] is True
