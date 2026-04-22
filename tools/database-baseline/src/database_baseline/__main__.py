import argparse
import pathlib
import sqlite3

from database_baseline.module_specs import MODULE_SPECS
from database_baseline.generate_sqlite import generate_for_sqlite_modules

if __name__ == "__main__":
    raise RuntimeError("You should use either main_postgresql() or main_sqlite()")

def main_postgresql() -> None:
    raise NotImplementedError("not implemented")

def main_sqlite() -> None:
    parser = argparse.ArgumentParser(description="Generate module init SQL files from a reference sqlite database.")
    parser.add_argument("--db-path", required=True, help="Path to the sqlite database already migrated to latest.")
    args = parser.parse_args()

    db_path = pathlib.Path(args.db_path).expanduser().resolve()
    if not db_path.exists():
        raise RuntimeError(f"Database path does not exist: {db_path}")

    repo_root = pathlib.Path(__file__).resolve().parents[4]

    with sqlite3.connect(str(db_path)) as connection:
        connection.row_factory = sqlite3.Row

        def output_callback(script: str, output_path: pathlib.Path) -> None:
            resolved_output_path = repo_root / output_path
            resolved_output_path.parent.mkdir(parents=True, exist_ok=True)
            resolved_output_path.write_text(script, encoding="utf-8")
            print(f"Generated {resolved_output_path}")

        generate_for_sqlite_modules(connection, output_callback, MODULE_SPECS)
