package io.medatarun.auth.infra.db

import io.medatarun.auth.infra.db.migrations.V002_CreateActorSystemMaintenance
import io.medatarun.auth.infra.db.migrations.V002_SeedRolesAndActorAssignmentsSQLite
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.platform.db.DbDialect
import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbMigrationContext
import io.medatarun.security.SecurityPermissionRegistry

class AuthDbMigration(
    private val securityPermissionRegistry: SecurityPermissionRegistry,
    private val actorStorageSQLite: ActorStorageSQLite,
    private val authClock: AuthClock
) : DbMigration {
    override val pluginId: String = "platform-auth"
    private val migrationV002CreateActorSystemMaintenance = V002_CreateActorSystemMaintenance()
    private val migrationV002SeedRolesAndActorAssignments = V002_SeedRolesAndActorAssignmentsSQLite(authClock)

    override fun install(ctx: DbMigrationContext) {
        when (ctx.dialect) {
            DbDialect.SQLITE -> ctx.applySqlResource(init_auth_sqlite)
            DbDialect.POSTGRESQL -> ctx.applySqlResource(init_auth_postgresql)
        }
    }

    override fun latestVersion(): Int {
        return 4
    }

    override fun applyVersion(version: Int, ctx: DbMigrationContext) {
        when (version) {
            1 -> listOf(
                // Only SQLite exists in this version
                // Matches version 0.8.0
                v001_01_users_sqlite, v001_02_oidc_sqlite, v001_03_actors_sqlite
            ).forEach { ctx.applySqlResource(it) }

            2 -> {
                // Only SQLite exists in this version
                // Matches version 0.8.0
                ctx.applySqlResource(v002_01_ids_binary16_sqlite)
                ctx.applySqlResource(v002_02_roles_sqlite)
                migrationV002SeedRolesAndActorAssignments.apply(ctx)
                ctx.applySqlResource(v002_03_drop_actor_roles_json_sqlite)
                ctx.applySqlResource(v002_04_client)
                ctx.applySqlResource(v002_05_rewrite_internal_issuer_sqlite)
                migrationV002CreateActorSystemMaintenance.apply(ctx)

            }

            3 -> {
                // Matches version 0.10.0
                when (ctx.dialect) {
                    DbDialect.SQLITE -> ctx.applySqlResource(v003_01_role_auto_assign_sqlite)
                    DbDialect.POSTGRESQL -> ctx.applySqlResource(v003_01_role_auto_assign_postgresql)
                }
            }

            4 -> {
                // Matches version 0.11.0
                when (ctx.dialect) {
                    DbDialect.SQLITE -> ctx.applySqlResource(v004_01_refresh_token_sqlite)
                    DbDialect.POSTGRESQL -> ctx.applySqlResource(v004_01_refresh_token_postgresql)
                }
            }

            else -> ctx.throwUnknownVersionException()
        }
    }

    override fun applyAlwaysAfterMigrations(ctx: DbMigrationContext) {
        val renamed = securityPermissionRegistry.findAllRenamed()
        if (renamed.isEmpty()) return
        actorStorageSQLite.renamePermissions(renamed)
    }

    @Suppress("ConstPropertyName")
    companion object {
        //@formatter:off
        const val init_auth_sqlite = "io/medatarun/auth/infra/db/init__auth_sqlite.sql"
        const val init_auth_postgresql = "io/medatarun/auth/infra/db/init__auth_postgresql.sql"
        const val v001_01_users_sqlite = "io/medatarun/auth/infra/db/version_auth_v001_01_users_sqlite.sql"
        const val v001_02_oidc_sqlite = "io/medatarun/auth/infra/db/version_auth_v001_02_oidc_sqlite.sql"
        const val v001_03_actors_sqlite = "io/medatarun/auth/infra/db/version_auth_v001_03_actors_sqlite.sql"
        const val v002_01_ids_binary16_sqlite = "io/medatarun/auth/infra/db/version_auth_v002_01_ids_binary16_sqlite.sql"
        const val v002_02_roles_sqlite = "io/medatarun/auth/infra/db/version_auth_v002_02_roles_sqlite.sql"
        const val v002_03_drop_actor_roles_json_sqlite = "io/medatarun/auth/infra/db/version_auth_v002_03_drop_actor_roles_sqlite.sql"
        const val v002_04_client = "io/medatarun/auth/infra/db/version_auth_v002_04_client_sqlite.sql"
        const val v002_05_rewrite_internal_issuer_sqlite = "io/medatarun/auth/infra/db/version_auth_v002_05_rewrite_internal_issuer_sqlite.sql"
        const val v003_01_role_auto_assign_sqlite = "io/medatarun/auth/infra/db/version_auth_v003_01_role_auto_assign_sqlite.sql"
        const val v003_01_role_auto_assign_postgresql = "io/medatarun/auth/infra/db/version_auth_v003_01_role_auto_assign_postgresql.sql"
        const val v004_01_refresh_token_sqlite = "io/medatarun/auth/infra/db/version_auth_v004_01_refresh_token_sqlite.sql"
        const val v004_01_refresh_token_postgresql = "io/medatarun/auth/infra/db/version_auth_v004_01_refresh_token_postgresql.sql"
        //@formatter:on
    }
}
