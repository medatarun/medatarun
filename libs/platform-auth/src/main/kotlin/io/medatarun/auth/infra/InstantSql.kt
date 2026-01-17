package io.medatarun.auth.infra

import io.medatarun.lang.exceptions.MedatarunException
import java.sql.ResultSet
import java.time.Instant
import java.time.format.DateTimeParseException

object InstantSql {

    fun fromSqlOptional(rs: ResultSet, column: String): Instant? {
        val rawValue = rs.getString(column) ?: return null
        return parseValue(rawValue, column)
    }

    fun fromSqlRequired(rs: ResultSet, column: String): Instant {
        val rawValue = rs.getString(column) ?: throw InstantSqlNullException(column)
        return parseValue(rawValue, column)
    }

    fun toSql(value: Instant): String {
        return value.toString()
    }

    private fun parseValue(rawValue: String, column: String): Instant {
        try {
            return Instant.parse(rawValue)
        } catch (e: DateTimeParseException) {
            // Wrap parsing issues to keep storage errors aligned with MedatarunException.
            throw InstantSqlParseException(column, rawValue, e)
        }
    }
}

class InstantSqlNullException(column: String) :
    MedatarunException("Missing Instant value for column '$column'.")

class InstantSqlParseException(column: String, value: String, cause: Throwable) :
    MedatarunException("Invalid Instant format for column '$column': $value") {
    init {
        initCause(cause)
    }
}
