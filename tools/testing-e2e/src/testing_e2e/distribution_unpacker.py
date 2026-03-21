from __future__ import annotations

import zipfile
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class DistributionUnpacker:
    archive_path: Path
    destination_dir: Path

    def unpack(self) -> Path:
        self.destination_dir.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(self.archive_path) as archive:
            archive.extractall(self.destination_dir)

        root_dir = self._find_distribution_root(self.destination_dir)
        self._make_executable(root_dir / "medatarun")
        bin_dir = root_dir / "bin"
        if bin_dir.exists():
            for entry in bin_dir.iterdir():
                if entry.is_file():
                    self._make_executable(entry)
        return root_dir

    @staticmethod
    def _find_distribution_root(destination_dir: Path) -> Path:
        entries = [entry for entry in destination_dir.iterdir() if entry.is_dir()]
        if len(entries) != 1:
            raise RuntimeError(f"Expected one distribution root under {destination_dir}, found {len(entries)}")
        return entries[0]

    @staticmethod
    def _make_executable(path: Path) -> None:
        path.chmod(path.stat().st_mode | 0o111)
