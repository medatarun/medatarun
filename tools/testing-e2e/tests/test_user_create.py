from __future__ import annotations

import secrets

import pytest

from testing_e2e.run_config import RunConfig
from testing_e2e.scenario import ClientVariant, SecretVariant
from testing_e2e.test_environment import TestEnvironment


USER_CREATE_CLIENTS = [
    pytest.param(ClientVariant.API, id="api"),
    pytest.param(ClientVariant.CLI_CONTAINER, id="cli-container"),
    pytest.param(ClientVariant.CLI_TEST_ENV, id="cli-test-env"),
]


@pytest.mark.parametrize("client_variant", USER_CREATE_CLIENTS)
def test_user_create(run_config: RunConfig, client_variant: ClientVariant) -> None:
    with TestEnvironment(run_config, client_variant, SecretVariant.PREDEFINED) as env:
        client = env.client()

        admin_username = "admin"
        admin_fullname = "Administrator"
        admin_password = secrets.token_urlsafe(24)
        bootstrap_secret = env.application.bootstrap_secret

        # Boostrap and create admin

        bootstrap_result = client.admin_bootstrap(admin_username, admin_fullname, admin_password, bootstrap_secret)
        assert bootstrap_result.is_status_code(200)

        admin_login_result = client.login(admin_username, admin_password)
        assert admin_login_result.is_status_code(200)
        admin_access_token = admin_login_result.json()["access_token"]

        # Checks that admin can log in

        whoami_result = client.whoami(admin_access_token)
        assert whoami_result.is_status_code(200)
        whoami = whoami_result.json()
        assert whoami["sub"] == admin_username
        assert whoami["admin"] is True

        user_username = "john.doe"
        user_fullname = "John Doe"
        user_password = secrets.token_urlsafe(24)

        # Create John

        user_create_result = client.user_create(
            user_username,
            user_fullname,
            user_password,
            False,
            admin_access_token,
        )
        assert user_create_result.is_status_code(200)

        # Checks that John is in actor list.

        actors_result = client.actor_list(admin_access_token)
        assert actors_result.is_status_code(200)
        actors = actors_result.json()
        assert any(actor["subject"] == admin_username for actor in actors)
        assert any(actor["subject"] == user_username for actor in actors)

        # Checks that John can log in and whoami returns the right actor.

        john_login_result = client.login(user_username, user_password)
        assert john_login_result.is_status_code(200)
        john_access_token = john_login_result.json()["access_token"]

        john_whoami_result = client.whoami(john_access_token)
        assert john_whoami_result.is_status_code(200)
        john_whoami = john_whoami_result.json()
        assert john_whoami["sub"] == user_username
        assert john_whoami["admin"] is False
