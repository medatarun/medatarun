# Automation e2e testing

## Quick start with uv

Install `uv` first if it is not already available on your machine. See https://docs.astral.sh/uv/

Then initialize a local environment for this project and install the test
dependencies:

```bash
cd tools/testing-e2e
uv venv
uv sync --extra test
```

Run the tests with:

```bash
uv run pytest
```

## Arguments

`testing-e2e.py` takes the following arguments:

- either `--from-gradle`
- or `--from-dist-url=<url>`

## Determining the artifact location

If `--from-gradle` is used, the `MEDATARUN_SRC` environment variable must be
defined, otherwise the process fails and stops.
`$MEDATARUN_SRC` must point to the source code of the project where a
`./gradlew` file is present, otherwise it is an error. We run `./gradlew build`
with `VERSION=test` in the environment. This produces
`$MEDATARUN_SRC/app/build/distributions/medatarun-${VERSION}.zip`

If `--from-dist-url=<url>` is used, then `<url>` is where an already
downloadable Medatarun `.zip` artifact can be found.

# Running the application

The application is always run in Docker with TestContainers.

The `.zip` artifact must be unzipped into `/opt/medatarun` (called
`$MEDATARUN_HOME` below).

To start the server, run `$MEDATARUN_HOME/medatarun serve`

We know the application is running when the logs show:
`Starting REST API on http://0.0.0.0:8080 with publicBaseUrl=`

The various tests must be able to provide environment variables to pass to the
container depending on their needs.

# Command line

When tests need to run the `medatarun` CLI, there are two ways to proceed.

- either run the CLI directly in the container: `$MEDATARUN_HOME/medatarun`
- or run the CLI from the test environment, in which case:
  - the `.zip` artifact must also be unzipped locally in a temporary directory
    (called `$MEDATARUN_TEST_CLI_HOME` here)
  - run `$MEDATARUN_TEST_CLI_HOME/medatarun` while pointing it to the Docker
    URL that serves the application
  - the connection parameters are described in
    [configuration.md](../../documentation/docs/installation/configuration.md)

Other instructions are in
[cli-usage.mdx](../../documentation/docs/usages/cli-usage.mdx)

# API

Instructions for using the API are in
[api-usage.mdx](../../documentation/docs/usages/api-usage.mdx)

# Tests to implement

The tests use pytest

## Admin user creation

This test comes in several variants:

- client variants:
  - API
  - CLI in the container
  - CLI in the test environment
- secret variants:
  - secret in the logs: run the test without the environment variable
    `$MEDATARUN_AUTH_BOOTSTRAP_SECRET` already set. The secret must be read
    from the Docker logs. Look for the log message
    `BOOTSTRAP SECRET (one-time usage): $secret`, where `$secret` is the secret
    to extract
  - predefined secret: run the test with the environment variable
    `$MEDATARUN_AUTH_BOOTSTRAP_SECRET` already set


## Bootstrap invalid secret


## Bootstrap consumed
