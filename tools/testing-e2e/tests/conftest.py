"""
This file must be named exactly "conftest.py" so pytest can discover it
"""
from __future__ import annotations

import pytest

from testing_e2e.run_config import RunConfig, load_run_config


def pytest_addoption(parser: pytest.Parser) -> None:
    """Register the command-line options used to choose the distribution source.

    Pytest calls this hook while loading the test suite. The options become
    available to `pytestconfig` and are consumed by `load_run_config()`.

    See documentation:
    https://docs.pytest.org/en/stable/reference/reference.html#std-hook-pytest_addoption
    """
    parser.addoption("--from-gradle", action="store_true", help="Build the distribution from the Medatarun source tree")
    parser.addoption("--from-dist-url", action="store", help="Path or URL to a Medatarun distribution zip")


@pytest.fixture(scope="session")
def run_config(pytestconfig: pytest.Config) -> RunConfig:
    """Parse the distribution source once for the whole test session."""
    return load_run_config(pytestconfig)
