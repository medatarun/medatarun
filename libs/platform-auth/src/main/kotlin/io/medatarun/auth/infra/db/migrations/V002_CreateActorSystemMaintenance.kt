package io.medatarun.auth.infra.db.migrations

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.DbDialect
import io.medatarun.platform.db.DbMigrationContext
import io.medatarun.platform.db.jdbc.setInstant
import io.medatarun.security.AppActorSystemMaintenance
import java.sql.Types

class V002_CreateActorSystemMaintenance {
    /**
     * Inserts the fixed system-maintenance actor.
     *
     * The insert is intentionally strict and lets SQL conflicts fail startup.
     */
    fun apply(ctx: DbMigrationContext) {
        val createdAt = UuidUtils.getInstant(AppActorSystemMaintenance.SYSTEM_MAINTENANCE_ACTOR_ID)
        ctx.withConnection { connection ->
            connection.prepareStatement(
                """
                INSERT INTO actors (
                    id, issuer, subject, full_name, email, roles_json, disabled_date, created_at, last_seen_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
            ).use { statement ->
                when (ctx.dialect) {
                    DbDialect.SQLITE -> {
                        statement.setString(1, AppActorSystemMaintenance.SYSTEM_MAINTENANCE_ACTOR_ID_STR)
                        statement.setString(6, "[]")
                    }
                    DbDialect.POSTGRESQL -> {
                        statement.setObject(1, AppActorSystemMaintenance.SYSTEM_MAINTENANCE_ACTOR_ID)
                        statement.setObject(6, "[]", Types.OTHER)
                    }
                }
                statement.setString(2, AppActorSystemMaintenance.SYSTEM_MAINTENANCE_ISSUER)
                statement.setString(3, AppActorSystemMaintenance.SYSTEM_MAINTENANCE_SUBJECT)
                statement.setString(4, AppActorSystemMaintenance.displayName)
                statement.setString(5, null)
                statement.setString(7, null)
                statement.setInstant(8, createdAt)
                statement.setInstant(9, createdAt)
                statement.executeUpdate()
            }
        }
    }
}
