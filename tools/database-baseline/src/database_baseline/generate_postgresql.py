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
        if module_spec.name == "auth":
            cleaned_script = cleaned_script + "\n" + build_auth_system_maintenance_insert_postgresql_sql()
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
        # Drop pg_dump comment blocks and metadata directives.
        if stripped_line.startswith("--"):
            continue
        if stripped_line.startswith("\\"):
            continue

        upper_line = stripped_line.upper()
        # Drop session bootstrap statements injected by pg_dump.
        if upper_line.startswith("SET ") and stripped_line.endswith(";"):
            continue
        if upper_line.startswith("SELECT PG_CATALOG.SET_CONFIG(") and stripped_line.endswith(";"):
            continue

        filtered_lines.append(line)

    compacted_lines = compact_consecutive_blank_lines(filtered_lines)
    cleaned_sql = "\n".join(compacted_lines).strip()
    if not cleaned_sql:
        raise RuntimeError("pg_dump output is empty after cleanup")

    # Generated scripts must not depend on an environment-specific schema name.
    quoted_schema_prefix_pattern = re.compile(rf'"{re.escape(schema)}"\.')
    unquoted_schema_prefix_pattern = re.compile(rf"\b{re.escape(schema)}\.")
    cleaned_sql = quoted_schema_prefix_pattern.sub("", cleaned_sql)
    cleaned_sql = unquoted_schema_prefix_pattern.sub("", cleaned_sql)

    return cleaned_sql + "\n"


def compact_consecutive_blank_lines(lines: list[str]) -> list[str]:
    """
    Keep at most one blank line between SQL statements to avoid noisy output.
    """
    compacted_lines: list[str] = []
    previous_was_blank = False
    for line in lines:
        current_is_blank = line.strip() == ""
        if current_is_blank and previous_was_blank:
            continue
        compacted_lines.append(line)
        previous_was_blank = current_is_blank
    return compacted_lines


def build_auth_system_maintenance_insert_postgresql_sql() -> str:
    """
    Seed the maintenance actor required by auth module initialization.
    """
    return (
        "INSERT INTO auth_actor (id, issuer, subject, full_name, email, disabled_date, created_at, last_seen_at)\n"
        "VALUES (\n"
        "    '01941f29-7c00-7000-9a65-67088ebcbabd',\n"
        "    'urn:medatarun:system',\n"
        "    'system-maintenance',\n"
        "    'System maintenance',\n"
        "    NULL,\n"
        "    NULL,\n"
        "    '2025-01-01T00:00:00Z',\n"
        "    '2025-01-01T00:00:00Z'\n"
        ");\n"
    )
