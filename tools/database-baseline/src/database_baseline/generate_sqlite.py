import pathlib
import sqlite3
from dataclasses import dataclass
from typing import Callable

from sqlfluff.api.simple import fix as sqlfluff_fix
from sqlfluff.core import FluffConfig

from database_baseline.utils.strip_simple_identifier_quotes import strip_simple_identifier_quotes

TableName = str
IndexName = str
CreateSql = str
OutputCallback = Callable[[str, pathlib.Path], None]


@dataclass(frozen=True)
class ModuleSpec:
    name: str
    table_names: tuple[str, ...]
    output_path: pathlib.Path


@dataclass(frozen=True)
class DbInspectionResult:
    """
    Snapshot of DDL objects read from sqlite_master.

    tables:
      key = table name from sqlite_master.name
      value = normalized CREATE TABLE statement

    indexes:
      key = index name from sqlite_master.name
      value = normalized CREATE INDEX statement
    """

    tables: dict[TableName, CreateSql]
    indexes: dict[IndexName, CreateSql]


MODULE_SPECS: tuple[ModuleSpec, ...] = (
    ModuleSpec(
        name="auth",
        table_names=(
            "auth_actor",
            "auth_actor_role",
            "auth_role",
            "auth_role_permission",
            "auth_client",
            "auth_code",
            "auth_ctx",
            "users",
        ),
        output_path=pathlib.Path(
            "libs/platform-auth/src/main/resources/io/medatarun/auth/infra/db/init__auth_sqlite.sql"
        ),
    ),
    ModuleSpec(
        name="models",
        table_names=(
            "model",
            "model_entity_attribute_snapshot",
            "model_entity_attribute_tag_snapshot",
            "model_entity_snapshot",
            "model_entity_tag_snapshot",
            "model_event",
            "model_relationship_attribute_snapshot",
            "model_relationship_attribute_tag_snapshot",
            "model_relationship_role_snapshot",
            "model_relationship_snapshot",
            "model_relationship_tag_snapshot",
            "model_search_item_snapshot",
            "model_search_item_tag_snapshot",
            "model_snapshot",
            "model_tag_snapshot",
            "model_type_snapshot",
        ),
        output_path=pathlib.Path(
            "extensions/models-core/src/main/resources/io/medatarun/model/infra/db/init__models_sqlite.sql"
        ),
    ),
    ModuleSpec(
        name="tags",
        table_names=(
            "tag_event",
            "tag_view_current_tag",
            "tag_view_current_tag_group",
            "tag_view_history_tag",
            "tag_view_history_tag_group",
        ),
        output_path=pathlib.Path(
            "extensions/tags-core/src/main/resources/io/medatarun/tags/core/infra/db/init__tags_sqlite.sql"
        ),
    ),
    ModuleSpec(
        name="actions",
        table_names=("action_audit_event",),
        output_path=pathlib.Path(
            "extensions/platform-actions-storage-db/src/main/resources/io/medatarun/actions/infra/db/init__actions_sqlite.sql"
        ),
    ),
)

SYSTEM_MAINTENANCE_ISSUER = "urn:medatarun:system"
SYSTEM_MAINTENANCE_SUBJECT = "system-maintenance"
SQLFLUFF_CONFIG = FluffConfig.from_strings(
    """
    [sqlfluff]
    dialect = sqlite
    max_line_length = 140
    """
)


def generate_for_sqlite_modules(
        connection: sqlite3.Connection,
        output_callback: OutputCallback,
        module_specs: tuple[ModuleSpec, ...]
) -> None:
    db_inspection_result: DbInspectionResult = inspect_database(connection)
    ensure_all_tables_mapped(db_inspection_result, module_specs)
    for module_spec in module_specs:
        script = build_module_script(connection, db_inspection_result, module_spec)
        formatted_script = format_sql_with_sqlfluff(script)
        output_callback(formatted_script, module_spec.output_path)


def inspect_database(connection: sqlite3.Connection) -> DbInspectionResult:
    rows = connection.execute(
        """
        SELECT type, name, tbl_name, sql
        FROM sqlite_master
        WHERE type IN ('table'
            , 'index')
          AND name NOT LIKE 'sqlite_%'
          AND name != 'schema_version_history'
        ORDER BY type, name
        """
    ).fetchall()

    table_ddls: dict[TableName, CreateSql] = {}
    index_ddls: dict[IndexName, CreateSql] = {}
    for row in rows:
        object_type = str(row["type"])
        name = str(row["name"])
        ddl = row["sql"]
        if ddl is None:
            continue
        normalized_ddl = normalize_sql(str(ddl))
        if object_type == "table":
            table_ddls[name] = normalized_ddl
            continue
        if object_type == "index":
            index_ddls[name] = normalized_ddl
            continue
        raise RuntimeError(f"Unsupported sqlite object type: {object_type}")
    return DbInspectionResult(tables=table_ddls, indexes=index_ddls)


def ensure_all_tables_mapped(db_inspection_result: DbInspectionResult, module_specs: tuple[ModuleSpec, ...]) -> None:
    all_declared_tables = set()
    for module_spec in module_specs:
        for table_name in module_spec.table_names:
            all_declared_tables.add(table_name)

    source_tables = set(db_inspection_result.tables.keys())
    unknown_tables = sorted(source_tables - all_declared_tables)
    if unknown_tables:
        raise RuntimeError(f"Source database contains unmapped tables: {', '.join(unknown_tables)}")

    missing_tables = sorted(all_declared_tables - source_tables)
    if missing_tables:
        raise RuntimeError(f"Mapped tables missing from source database: {', '.join(missing_tables)}")


def build_module_script(
        connection: sqlite3.Connection,
        db_inspection_result: DbInspectionResult,
        module_spec: ModuleSpec
) -> str:
    table_ddls: list[str] = []
    index_ddls: list[str] = []

    for table_name in sorted(module_spec.table_names):
        table_sql = db_inspection_result.tables.get(table_name)
        if table_sql is None:
            raise RuntimeError(f"Missing table SQL for [{table_name}] in module [{module_spec.name}]")
        table_ddls.append(strip_simple_identifier_quotes(table_sql))

    module_table_set = set(module_spec.table_names)
    for index_name in sorted(db_inspection_result.indexes.keys()):
        index_sql = db_inspection_result.indexes[index_name]
        table_name = find_index_table_name(index_sql)
        if table_name in module_table_set:
            index_ddls.append(strip_simple_identifier_quotes(index_sql))

    parts: list[str] = []
    parts.extend(table_ddls)
    if index_ddls:
        parts.append("\n".join(index_ddls))
    if module_spec.name == "auth":
        parts.append(build_auth_system_maintenance_insert_sql(connection))
    return "\n\n".join(parts).strip() + "\n"


def find_index_table_name(index_sql: str) -> str:
    create_index_prefix = " ON "
    index_position = index_sql.upper().find(create_index_prefix)
    if index_position < 0:
        raise RuntimeError(f"Unable to parse index SQL: {index_sql}")
    table_segment = index_sql[index_position + len(create_index_prefix):]
    paren_position = table_segment.find("(")
    if paren_position < 0:
        raise RuntimeError(f"Unable to parse index SQL: {index_sql}")
    return table_segment[:paren_position].strip().strip("\"")


def build_auth_system_maintenance_insert_sql(connection: sqlite3.Connection) -> str:
    row = connection.execute(
        """
        SELECT id,
               issuer,
               subject,
               full_name,
               email,
               disabled_date,
               created_at,
               last_seen_at
        FROM auth_actor
        WHERE issuer = ?
          AND subject = ?
        """,
        (SYSTEM_MAINTENANCE_ISSUER, SYSTEM_MAINTENANCE_SUBJECT),
    ).fetchone()
    if row is None:
        raise RuntimeError("System maintenance actor row was not found in source database.")

    actor_id = row["id"]
    if not isinstance(actor_id, bytes):
        raise RuntimeError("System maintenance actor id must be a sqlite BLOB.")
    actor_id_sql = f"X'{actor_id.hex().upper()}'"

    values = [
        actor_id_sql,
        quote_sql_string(row["issuer"]),
        quote_sql_string(row["subject"]),
        quote_sql_string(row["full_name"]),
        quote_nullable_sql_string(row["email"]),
        quote_nullable_sql_string(row["disabled_date"]),
        quote_sql_string(row["created_at"]),
        quote_sql_string(row["last_seen_at"]),
    ]

    return (
        "INSERT INTO auth_actor (id, issuer, subject, full_name, email, disabled_date, created_at, last_seen_at)\n"
        f"VALUES ({', '.join(values)});"
    )


def quote_sql_string(value: object) -> str:
    if not isinstance(value, str):
        raise RuntimeError(f"Expected sqlite TEXT value, got: {value!r}")
    return "'" + value.replace("'", "''") + "'"


def quote_nullable_sql_string(value: object) -> str:
    if value is None:
        return "NULL"
    return quote_sql_string(value)


def normalize_sql(sql: str) -> str:
    text = sql.strip()
    if not text.endswith(";"):
        text = text + ";"
    return text


def format_sql_with_sqlfluff(sql: str) -> str:
    formatted_sql = sqlfluff_fix(sql, config=SQLFLUFF_CONFIG)
    if not formatted_sql.endswith("\n"):
        return formatted_sql + "\n"
    return formatted_sql
