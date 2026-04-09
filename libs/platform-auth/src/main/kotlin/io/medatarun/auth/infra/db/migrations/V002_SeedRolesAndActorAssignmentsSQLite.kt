package io.medatarun.auth.infra.db.migrations

import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.DbMigrationContext
import io.medatarun.platform.db.jdbc.setInstantSQLite
import io.medatarun.platform.db.jdbc.setUUID

class V002_SeedRolesAndActorAssignmentsSQLite(
    private val authClock: AuthClock
) {
    /**
     * Seeds initial auth roles and maps legacy actor roles_json entries into auth_actor_role.
     */
    fun apply(ctx: DbMigrationContext) {
        val now = authClock.now()
        val adminRoleId = UuidUtils.generateV7()
        val managerRoleId = UuidUtils.generateV7()

        ctx.withConnection { connection ->
            connection.prepareStatement(
                """
                INSERT INTO auth_role (id, key, name, description, created_at, last_updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent()
            ).use { statement ->
                statement.setUUID(1, adminRoleId)
                statement.setString(2, "admin")
                statement.setString(3, "Admin")
                statement.setString(4, null)
                statement.setInstantSQLite(5, now)
                statement.setInstantSQLite(6, now)
                statement.executeUpdate()

                statement.setUUID(1, managerRoleId)
                statement.setString(2, "manager")
                statement.setString(3, "Manager")
                statement.setString(4, "Initial role created by the system. You can discard it if not needed.")
                statement.setInstantSQLite(5, now)
                statement.setInstantSQLite(6, now)
                statement.executeUpdate()
            }

            connection.prepareStatement(
                """
                INSERT INTO auth_role_permission (auth_role_id, permission)
                VALUES (?, ?)
                """.trimIndent()
            ).use { statement ->
                statement.setUUID(1, adminRoleId)
                statement.setString(2, "admin")
                statement.executeUpdate()

                statement.setUUID(1, managerRoleId)
                statement.setString(2, "tag_global_manage")
                statement.executeUpdate()
                statement.setString(2, "tag_local_manage")
                statement.executeUpdate()
                statement.setString(2, "tag_group_manage")
                statement.executeUpdate()
            }

            connection.prepareStatement(
                """
                INSERT OR IGNORE INTO auth_actor_role (auth_actor_id, auth_role_id)
                SELECT id, ?
                FROM auth_actor
                WHERE roles_json LIKE ?
                """.trimIndent()
            ).use { statement ->
                statement.setUUID(1, adminRoleId)
                statement.setString(2, "%\"admin\"%")
                statement.executeUpdate()
            }

            connection.prepareStatement(
                """
                INSERT OR IGNORE INTO auth_actor_role (auth_actor_id, auth_role_id)
                SELECT id, ?
                FROM auth_actor
                WHERE roles_json LIKE ?
                   OR roles_json LIKE ?
                   OR roles_json LIKE ?
                """.trimIndent()
            ).use { statement ->
                statement.setUUID(1, managerRoleId)
                statement.setString(2, "%\"tag_global_manage\"%")
                statement.setString(3, "%\"tag_local_manage\"%")
                statement.setString(4, "%\"tag_group_manage\"%")
                statement.executeUpdate()
            }
        }
    }
}
