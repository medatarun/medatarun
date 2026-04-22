import os
import pathlib
import re
import subprocess
from typing import Callable

from database_baseline.module_specs import ModuleSpec
from database_baseline.utils.sqlfluff_formatter import format_sqlite_with_sqlfluff

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
        cleaned_script = cleanup_pg_dump_output(script, schema)
        formatted_script = format_sqlite_with_sqlfluff(cleaned_script, "postgres")
        output_callback(formatted_script, module_spec.output_path_postgresql)


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


def cleanup_pg_dump_output(sql: str, schema: str) -> str:
    """
    Remove pg_dump boilerplate statements and comments, then drop schema qualifiers.
    The generated module scripts keep only portable DDL statements.
    """
    filtered_lines: list[str] = []
    for line in sql.splitlines():
        stripped_line = line.strip()
        if stripped_line.startswith("--"):
            continue
        if stripped_line.startswith("\\"):
            continue

        upper_line = stripped_line.upper()
        if upper_line.startswith("SET ") and stripped_line.endswith(";"):
            continue
        if upper_line.startswith("SELECT PG_CATALOG.SET_CONFIG(") and stripped_line.endswith(";"):
            continue

        filtered_lines.append(line)

    cleaned_sql = "\n".join(filtered_lines).strip()
    if not cleaned_sql:
        raise RuntimeError("pg_dump output is empty after cleanup")

    quoted_schema_prefix_pattern = re.compile(rf'"{re.escape(schema)}"\.')
    unquoted_schema_prefix_pattern = re.compile(rf"\b{re.escape(schema)}\.")
    cleaned_sql = quoted_schema_prefix_pattern.sub("", cleaned_sql)
    cleaned_sql = unquoted_schema_prefix_pattern.sub("", cleaned_sql)

    return cleaned_sql + "\n"
