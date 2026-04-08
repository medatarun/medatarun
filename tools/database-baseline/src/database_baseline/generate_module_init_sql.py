import argparse
import pathlib
import sqlite3

from database_baseline.generate_sqlite import MODULE_SPECS
from database_baseline.generate_sqlite import generate_for_sqlite_modules


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate module init SQL files from a reference sqlite database.")
    parser.add_argument("--db-path", required=True, help="Path to the sqlite database already migrated to latest.")
    parser.add_argument(
        "--dialect",
        required=True,
        choices=("sqlite", "postgresql"),
        help="Dialect to generate.",
    )
    args = parser.parse_args()

    db_path = pathlib.Path(args.db_path).expanduser().resolve()
    if not db_path.exists():
        raise RuntimeError(f"Database path does not exist: {db_path}")

    repo_root = pathlib.Path(__file__).resolve().parents[4]
    if args.dialect == "sqlite":
        generate_for_sqlite(repo_root, db_path)
        return
    if args.dialect == "postgresql":
        generate_for_postgresql(repo_root, db_path)
        return
    raise RuntimeError(f"Unsupported dialect: {args.dialect}")


def generate_for_sqlite(repo_root: pathlib.Path, db_path: pathlib.Path) -> None:
    with sqlite3.connect(str(db_path)) as connection:
        connection.row_factory = sqlite3.Row

        def output_callback(script: str, output_path: pathlib.Path) -> None:
            resolved_output_path = repo_root / output_path
            resolved_output_path.parent.mkdir(parents=True, exist_ok=True)
            resolved_output_path.write_text(script, encoding="utf-8")
            print(f"Generated {resolved_output_path}")

        generate_for_sqlite_modules(connection, output_callback, MODULE_SPECS)


def generate_for_postgresql(repo_root: pathlib.Path, db_path: pathlib.Path) -> None:
    raise NotImplementedError("not implemented")
