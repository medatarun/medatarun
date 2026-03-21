from __future__ import annotations

import secrets

import pytest

from testing_e2e.run_config import RunConfig
from testing_e2e.scenario import ClientVariant, SecretVariant
from testing_e2e.test_environment import TestEnvironment


INVALID_BOOTSTRAP_CLIENTS = [
    pytest.param(ClientVariant.API, id="api"),
    pytest.param(ClientVariant.CLI_CONTAINER, id="cli-container"),
    pytest.param(ClientVariant.CLI_TEST_ENV, id="cli-test-env"),
]

# Boostrap secret invalid
#
# - Start the application with a predefined secret
# - Verify that the predefined secret appears in the logs
# - create an admin with a wrong secret (depending on the client variant)
#   `medatarun auth admin_bootstrap --username=admin --fullname="Administrator" --password="..." --secret=...`
# - verify that the error `Bad bootstrap secret` is returned, depending on the
#   client variant
# - request an access token (depending on the client variant)
#   `medatarun auth login --username=admin --password="..."`, which must return a
#   JSON object with `title = Bad credentials`

@pytest.mark.parametrize("client_variant", INVALID_BOOTSTRAP_CLIENTS)
def test_bootstrap_invalid_secret(run_config: RunConfig, client_variant: ClientVariant) -> None:
    with TestEnvironment(run_config, client_variant, SecretVariant.PREDEFINED) as env:
        client = env.client()

        correct_secret = env.application.bootstrap_secret
        assert correct_secret in env.application.logs_text()

        username = "admin"
        fullname = "Administrator"
        password = secrets.token_urlsafe(24)
        wrong_secret = correct_secret + ".wrong"

        bootstrap_result = client.admin_bootstrap(username, fullname, password, wrong_secret)
        assert bootstrap_result.is_status_code(401)
        assert bootstrap_result.has_error_text("Bad bootstrap secret.")

        login_result = client.login(username, password)
        assert login_result.is_status_code(401)
        assert login_result.has_error_text("Bad credentials.")
