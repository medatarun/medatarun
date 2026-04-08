from database_baseline.utils.strip_simple_identifier_quotes import strip_simple_identifier_quotes


class TestStripSimpleIdentifierQuotes:
    def test_removes_quotes_on_simple_identifiers(self) -> None:
        source = 'CREATE TABLE "auth_actor" ("id" TEXT, "issuer" TEXT);'

        result = strip_simple_identifier_quotes(source)

        assert result == "CREATE TABLE auth_actor (id TEXT, issuer TEXT);"

    def test_keeps_text_literals_unchanged(self) -> None:
        source = """CREATE TABLE "auth_actor" ("id" TEXT, note TEXT DEFAULT 'x"y', payload TEXT DEFAULT 'a''b');"""

        result = strip_simple_identifier_quotes(source)

        assert result == """CREATE TABLE auth_actor (id TEXT, note TEXT DEFAULT 'x"y', payload TEXT DEFAULT 'a''b');"""

    def test_keeps_complex_quoted_identifiers(self) -> None:
        source = 'CREATE TABLE "auth actor" ("id" TEXT, "regular_name" TEXT);'

        result = strip_simple_identifier_quotes(source)

        assert result == 'CREATE TABLE "auth actor" (id TEXT, regular_name TEXT);'
