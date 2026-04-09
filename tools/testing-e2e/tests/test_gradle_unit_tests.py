from __future__ import annotations

import os
import subprocess

import pytest

from testing_e2e.run_config import GradleRunConfig, RunConfig


def test_gradle_unit_tests_sqlite(run_config: RunConfig) -> None:
    """Run Gradle unit tests on SQLite from Gradle sources."""
    if not isinstance(run_config, GradleRunConfig):
        pytest.skip("This test is only applicable when the suite runs with --from-gradle")

    env = os.environ.copy()
    # `--rerun-tasks` avoids "UP-TO-DATE"/cache reuse so unit tests are executed again.
    subprocess.run(["./gradlew", "test", "--rerun-tasks"], cwd=run_config.project_root, env=env, check=True)

def test_gradle_unit_tests_postgresql(run_config: RunConfig) -> None:
    """Run Gradle unit tests on Postgresql via gradle > Junit > TestContainers from Gradle sources."""
    if not isinstance(run_config, GradleRunConfig):
        pytest.skip("This test is only applicable when the suite runs with --from-gradle")

    env = os.environ.copy()
    env["MEDATARUN_STORAGE_DATASOURCE_JDBC_DBENGINE"]="postgresql"
    # `--rerun-tasks` avoids "UP-TO-DATE"/cache reuse so unit tests are executed again.
    subprocess.run(["./gradlew", "test", "--rerun-tasks"], cwd=run_config.project_root, env=env, check=True)
