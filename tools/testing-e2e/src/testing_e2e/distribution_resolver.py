from __future__ import annotations

import hashlib
import os
import subprocess
import urllib.parse
import urllib.request
from dataclasses import dataclass
from pathlib import Path
import logging

from testing_e2e.run_config import DistributionUrlRunConfig, GradleRunConfig, RunConfig

logger = logging.getLogger(__name__)

@dataclass(frozen=True)
class DistributionResolver:
    """
    Resolves the .zip file of the project. 
    If configuration is gradle, then build the project locally, 
    else download the .zip from web releases
    """
    config: RunConfig

    def resolve(self) -> Path:
        logger.info("Resolve distribution .zip")
        if isinstance(self.config, GradleRunConfig):
            return self._resolve_gradle(self.config)
        if isinstance(self.config, DistributionUrlRunConfig):
            return self._resolve_url(self.config)
        raise RuntimeError(f"Unsupported distribution source: {self.config}")

    def _resolve_gradle(self, config:GradleRunConfig) -> Path:
        """Build project with gradle then get the .zip file"""

        logger.info("Build from gradle")
        project_root = config.project_root
        gradlew = project_root / "gradlew"
        if not gradlew.exists():
            raise RuntimeError(f"Gradle wrapper not found in {project_root}")
        env = os.environ.copy()
        subprocess.run(["./gradlew", "build"], cwd=project_root, env=env, check=True)
        return project_root / "app" / "build" / "distributions" / "medatarun-dev.zip"

    def _resolve_url(self,config: DistributionUrlRunConfig) -> Path:
        """Download .zip file from distribution URL"""

        logger.info("Download .zip from releases")
        distribution_url = config.distribution_url
        parsed = urllib.parse.urlparse(distribution_url)
        if parsed.scheme in {"http", "https"}:
            destination = self._cache_dir() / f"{hashlib.sha256(distribution_url.encode('utf-8')).hexdigest()}.zip"
            if not destination.exists():
                with urllib.request.urlopen(distribution_url) as response:
                    destination.write_bytes(response.read())
            return destination

        if parsed.scheme == "file":
            return Path(parsed.path)

        source_path = Path(distribution_url)
        if source_path.exists():
            return source_path

        raise RuntimeError(f"Unsupported distribution source: {distribution_url}")

    @staticmethod
    def _cache_dir() -> Path:
        # Keep downloaded archives outside the temporary test workspace so they
        # survive between test runs and do not get deleted with the workspace.
        cache_root = Path.home() / ".cache" / "medatarun" / "testing-e2e"
        cache_root.mkdir(parents=True, exist_ok=True)
        return cache_root
