import re

SIMPLE_SQL_IDENTIFIER_PATTERN = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*$")


def strip_simple_identifier_quotes(sql: str) -> str:
    """
    Normalize SQLite DDL by removing quotes around simple identifiers.

    Why this exists:
    - We read CREATE statements from the `sql` column of SQLite table `sqlite_master`
      (query: `SELECT type, name, tbl_name, sql FROM sqlite_master ...`), and SQLite stores
      that SQL text close to how statements were created or rewritten by ALTER TABLE operations.
    - The same database can therefore contain a mix of styles:
      `CREATE TABLE "auth_actor"` and `CREATE TABLE auth_role`.
    - Our generator copies this SQL into module init files, so that mixed style leaks into
      generated files unless we normalize.

    What this function does:
    - It removes only `"` around simple identifiers such as `"auth_actor"` or `"id"`.
    - It keeps complex quoted identifiers unchanged (for example `"auth actor"`).
    - It does not touch SQL text literals (`'...'`), because those may legitimately contain
      double quotes that are data, not identifier delimiters.
    """
    chunks: list[str] = []
    position = 0
    text_length = len(sql)
    while position < text_length:
        current_char = sql[position]

        if current_char == "'":
            literal_end = find_sql_text_literal_end(sql, position)
            chunks.append(sql[position:literal_end])
            position = literal_end
            continue

        if current_char != "\"":
            chunks.append(current_char)
            position = position + 1
            continue

        quote_end = sql.find("\"", position + 1)
        if quote_end < 0:
            chunks.append(current_char)
            position = position + 1
            continue

        identifier = sql[position + 1:quote_end]
        if SIMPLE_SQL_IDENTIFIER_PATTERN.match(identifier) is not None:
            chunks.append(identifier)
        else:
            chunks.append(sql[position:quote_end + 1])
        position = quote_end + 1

    return "".join(chunks)


def find_sql_text_literal_end(sql: str, start_position: int) -> int:
    """
    Return the position just after the SQL text literal starting at `start_position`.

    SQL escapes a single quote inside a text literal with doubled quotes (`''`), so this
    scanner must skip those escape pairs instead of ending early.
    """
    literal_position = start_position + 1
    while literal_position < len(sql):
        if sql[literal_position] != "'":
            literal_position = literal_position + 1
            continue
        next_position = literal_position + 1
        if next_position < len(sql) and sql[next_position] == "'":
            literal_position = literal_position + 2
            continue
        return next_position
    return len(sql)
