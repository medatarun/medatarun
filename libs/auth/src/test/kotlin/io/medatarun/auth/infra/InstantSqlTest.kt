package io.medatarun.auth.infra

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InstantSqlTest {

    @Test
    fun toSqlUsesInstantStringFormat() {
        val instant = Instant.parse("2024-01-01T00:00:00Z")

        assertEquals("2024-01-01T00:00:00Z", InstantSql.toSql(instant))
    }

    @Test
    fun fromSqlReturnsNullWhenColumnIsNull() {
        val holder = createResultSetWithValue(null)
        try {
            assertNull(InstantSql.fromSqlOptional(holder.resultSet, "instant_value"))
        } finally {
            holder.close()
        }
    }

    @Test
    fun fromSqlRequiredThrowsWhenColumnIsNull() {
        val holder = createResultSetWithValue(null)
        try {
            assertThrows<InstantSqlNullException> {
                InstantSql.fromSqlRequired(holder.resultSet, "instant_value")
            }
        } finally {
            holder.close()
        }
    }

    @Test
    fun fromSqlRequiredThrowsOnInvalidFormat() {
        val holder = createResultSetWithValue("not-an-instant")
        try {
            assertThrows<InstantSqlParseException> {
                InstantSql.fromSqlRequired(holder.resultSet, "instant_value")
            }
        } finally {
            holder.close()
        }
    }

    @Test
    fun fromSqlRequiredReadsInstant() {
        val holder = createResultSetWithValue("2024-02-03T04:05:06Z")
        try {
            val parsed = InstantSql.fromSqlRequired(holder.resultSet, "instant_value")
            assertEquals(Instant.parse("2024-02-03T04:05:06Z"), parsed)
        } finally {
            holder.close()
        }
    }

    // Use a real in-memory SQLite ResultSet to exercise JDBC behavior.
    private fun createResultSetWithValue(value: String?): ResultSetHolder {
        val connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        connection.createStatement().use { statement ->
            statement.execute("CREATE TABLE sample (instant_value TEXT)")
        }
        connection.prepareStatement("INSERT INTO sample(instant_value) VALUES (?)").use { ps ->
            ps.setString(1, value)
            ps.executeUpdate()
        }
        val resultSet = connection.createStatement().executeQuery(
            "SELECT instant_value FROM sample LIMIT 1"
        )
        resultSet.next()
        return ResultSetHolder(connection, resultSet)
    }

    private class ResultSetHolder(
        private val connection: Connection,
        val resultSet: ResultSet
    ) {
        fun close() {
            resultSet.close()
            connection.close()
        }
    }
}
