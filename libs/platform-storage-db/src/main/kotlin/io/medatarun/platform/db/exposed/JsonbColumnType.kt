package io.medatarun.platform.db.exposed

import org.jetbrains.exposed.v1.core.TextColumnType
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.core.vendors.currentDialect

/**
 * Stores JSON payload as `JSONB` on PostgreSQL and as `TEXT` on dialects that
 * do not support JSONB (SQLite in this project).
 *
 * PostgreSQL parameters are explicitly cast to `jsonb` to avoid type mismatch
 * when values are bound as strings in prepared statements.
 */
class JsonbColumnType : TextColumnType() {
    override fun preciseType(): String {
        return if (currentDialect is PostgreSQLDialect) {
            currentDialect.dataTypeProvider.jsonBType()
        } else {
            currentDialect.dataTypeProvider.textType()
        }
    }

    override fun parameterMarker(value: String?): String {
        return if (currentDialect is PostgreSQLDialect) {
            "?::jsonb"
        } else {
            "?"
        }
    }
}
