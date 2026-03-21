from __future__ import annotations

import hashlib
import os
import subprocess
import urllib.parse
import urllib.request
from dataclasses import dataclass
from pathlib import Path

from testing_e2e.run_config import DistributionUrlRunConfig, GradleRunConfig, RunConfig


@dataclass(frozen=True)
class DistributionResolver:
    config: RunConfig

    def resolve(self) -> Path:
        if isinstance(self.config, GradleRunConfig):
            return self._resolve_gradle()
        if isinstance(self.config, DistributionUrlRunConfig):
            return self._resolve_url()
        raise RuntimeError(f"Unsupported distribution source: {self.config}")

    def _resolve_gradle(self) -> Path:
        project_root = self.config.project_root
        gradlew = project_root / "gradlew"
        if not gradlew.exists():
            raise RuntimeError(f"Gradle wrapper not found in {project_root}")
        env = os.environ.copy()
        env["VERSION"] = "test"
        subprocess.run(["./gradlew", "build"], cwd=project_root, env=env, check=True)
        return project_root / "app" / "build" / "distributions" / "medatarun-dev.zip"

    def _resolve_url(self) -> Path:
        distribution_url = self.config.distribution_url
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
