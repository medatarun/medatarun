import argparse
import pathlib
import sqlite3

from database_baseline.module_specs import MODULE_SPECS
from database_baseline.generate_postgresql import generate_for_postgresql_modules
from database_baseline.generate_sqlite import generate_for_sqlite_modules

if __name__ == "__main__":
    raise RuntimeError("You should use either main_postgresql() or main_sqlite()")

def main_postgresql() -> None:
    parser = argparse.ArgumentParser(
        description="Generate module init SQL files from a reference PostgreSQL database."
    )
    parser.add_argument("--db-name", required=False, default="medatarun", help="PostgreSQL database name.")
    parser.add_argument("--db-host", required=False, default="localhost", help="PostgreSQL host.")
    parser.add_argument("--db-port", required=False, type=int, default=5432, help="PostgreSQL port.")
    parser.add_argument("--db-user", required=False, default="medatarun", help="PostgreSQL user.")
    parser.add_argument("--db-password", required=False, default="medatarun", help="PostgreSQL password.")
    parser.add_argument("--schema", required=False, default="medatarun_dev", help="PostgreSQL schema to inspect and export.")
    parser.add_argument("--docker-container", required=False, default="postgresql", help="Docker container name running PostgreSQL tools.")
    args = parser.parse_args()

    schema = args.schema.strip()
    if not schema:
        raise RuntimeError("Schema must be a non-empty value.")
    repo_root = pathlib.Path(__file__).resolve().parents[4]

    def output_callback(script: str, output_path: pathlib.Path) -> None:
        resolved_output_path = repo_root / output_path
        resolved_output_path.parent.mkdir(parents=True, exist_ok=True)
        resolved_output_path.write_text(script, encoding="utf-8")
        print(f"Generated {resolved_output_path}")

    generate_for_postgresql_modules(
        db_name=args.db_name,
        db_host=args.db_host,
        db_port=args.db_port,
        db_user=args.db_user,
        db_password=args.db_password,
        schema=schema,
        docker_container=args.docker_container,
        output_callback=output_callback,
        module_specs=MODULE_SPECS,
    )

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
