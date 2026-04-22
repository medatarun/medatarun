from sqlfluff.api.simple import fix as sqlfluff_fix
from sqlfluff.core import FluffConfig


def format_sqlite_with_sqlfluff(sql: str, dialect: str) -> str:
    config = FluffConfig.from_strings(
        f"""
        [sqlfluff]
        dialect = {dialect}
        max_line_length = 140
        """
    )

    formatted_sql = sqlfluff_fix(sql, config=config)
    if not formatted_sql.endswith("\n"):
        return formatted_sql + "\n"
    return formatted_sql
