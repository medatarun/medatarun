import os
import pathlib
import subprocess
from typing import Callable

from database_baseline.module_specs import ModuleSpec

OutputCallback = Callable[[str, pathlib.Path], None]


def generate_for_postgresql_modules(
    db_name: str,
    db_host: str,
    db_port: int,
    db_user: str,
    db_password: str,
    schema: str,
    docker_container: str,
    output_callback: OutputCallback,
    module_specs: tuple[ModuleSpec, ...],
) -> None:
    """
    Export one PostgreSQL schema-only dump per module using only the module table list.
    pg_dump is executed inside the provided Docker container.
    """
    for module_spec in module_specs:
        script = export_module_schema_sql(
            db_name=db_name,
            db_host=db_host,
            db_port=db_port,
            db_user=db_user,
            db_password=db_password,
            schema=schema,
            docker_container=docker_container,
            module_spec=module_spec,
        )
        output_callback(script, module_spec.output_path_postgresql)


def export_module_schema_sql(
    db_name: str,
    db_host: str,
    db_port: int,
    db_user: str,
    db_password: str,
    schema: str,
    docker_container: str,
    module_spec: ModuleSpec,
) -> str:
    command = [
        "docker",
        "exec",
        "--env",
        f"PGPASSWORD={db_password}",
        docker_container,
        "pg_dump",
        "--schema-only",
        "--no-owner",
        "--no-privileges",
        "--no-comments",
        "--no-tablespaces",
        "--no-table-access-method",
        "--host",
        db_host,
        "--port",
        str(db_port),
        "--dbname",
        db_name,
        "--username",
        db_user,
        "--schema",
        schema,
    ]

    for table_name in module_spec.table_names:
        command.extend(["--table", f"{schema}.{table_name}"])

    process = subprocess.run(command, capture_output=True, text=True, env=dict(os.environ))
    if process.returncode != 0:
        raise RuntimeError(
            f"pg_dump failed for module [{module_spec.name}] with code [{process.returncode}]: {process.stderr.strip()}"
        )

    sql = process.stdout.strip()
    if not sql:
        raise RuntimeError(f"pg_dump returned an empty output for module [{module_spec.name}]")
    return sql + "\n"
