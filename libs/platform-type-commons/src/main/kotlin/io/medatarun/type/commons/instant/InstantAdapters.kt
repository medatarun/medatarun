package io.medatarun.type.commons.instant

import java.sql.Timestamp
import java.time.Instant

object InstantAdapters {
    /**
     * Formats an [Instant] as SQLite-compatible timestamp text expected by JDBC timestamp parsing.
     *
     * Instant.toString() uses ISO-8601 with `T` and `Z` (example: 2026-03-30T09:33:54.555163Z), while
     * the SQLite JDBC parser for timestamp columns expects a SQL timestamp text (`yyyy-MM-dd HH:mm:ss.SSS...`).
     * Using Timestamp.from(instant).toString() keeps storage readable by Exposed timestamp columns.
     */
    fun toSqlTimestampString(instant: Instant): String {
        return Timestamp.from(instant).toString()
    }
}
