from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path

import pytest


@dataclass(frozen=True)
class GradleRunConfig:
    """Use the local Gradle project as the source of the distribution zip."""

    project_root: Path


@dataclass(frozen=True)
class DistributionUrlRunConfig:
    """Use a prebuilt distribution archive referenced by URL or path."""

    distribution_url: str


RunConfig = GradleRunConfig | DistributionUrlRunConfig


def load_run_config(pytestconfig: pytest.Config) -> RunConfig:
    """Read the distribution source from pytest options and validate the choice.

    The test suite accepts exactly one source:
    - `--from-gradle`, which resolves the archive from the local Gradle build
    - `--from-dist-url`, which uses the archive URL passed on the command line
    """
    from_gradle = bool(pytestconfig.getoption("--from-gradle"))
    dist_url = pytestconfig.getoption("--from-dist-url")

    if from_gradle and dist_url is not None:
        raise RuntimeError("Use either --from-gradle or --from-dist-url, not both")

    if not from_gradle and dist_url is None:
        from_gradle = True

    run_config: RunConfig
    if from_gradle:
        medatarun_src = os.environ.get("MEDATARUN_SRC")
        project_root:Path
        if medatarun_src is not None:
            project_root = Path(medatarun_src)
        else:
            project_root = Path(__file__).resolve().parents[4]
        print(f"Gradle location resolved {project_root}")

        if not (project_root / "settings.gradle.ks").exists:
            raise RuntimeError("Location doesnt point to medatarun gradle project")
    
        run_config = GradleRunConfig(Path(project_root))
    else:
        run_config = DistributionUrlRunConfig(dist_url)

    return run_config
