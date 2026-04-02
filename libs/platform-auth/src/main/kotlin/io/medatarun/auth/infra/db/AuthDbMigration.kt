package io.medatarun.auth.infra.db

import io.medatarun.auth.infra.db.migrations.V002_CreateActorSystemMaintenance
import io.medatarun.platform.db.DbDialect
import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbMigrationContext
import io.medatarun.security.SecurityRolesRegistry

class AuthDbMigration(
    private val securityRolesRegistry: SecurityRolesRegistry,
    private val actorStorageSQLite: ActorStorageSQLite
) : DbMigration {
    override val pluginId: String = "platform-auth"
    private val migrationV002CreateActorSystemMaintenance = V002_CreateActorSystemMaintenance()

    override fun install(ctx: DbMigrationContext) {
        when (ctx.dialect) {
            DbDialect.SQLITE -> ctx.applySqlResource(init_auth_sqlite)
            DbDialect.POSTGRESQL -> ctx.applySqlResource(init_auth_postgresql)
        }
    }

    override fun latestVersion(): Int {
        return 2
    }

    override fun applyVersion(version: Int, ctx: DbMigrationContext) {
        when (version) {
            1 -> listOf(v001_users, v001_oidc, v001_actors).forEach { ctx.applySqlResource(it) }
            2 -> {
                migrationV002CreateActorSystemMaintenance.apply(ctx)
                ctx.applySqlResource(v002_ids_binary16_sqlite)
                ctx.applySqlResource(v002_auth_client_sqlite)
            }
            else -> ctx.throwUnknownVersionException()
        }
    }

    override fun applyAlwaysAfterMigrations(ctx: DbMigrationContext) {
        val renamed = securityRolesRegistry.findAllRenamedRoles()
        if (renamed.isEmpty()) return
        actorStorageSQLite.renameRoles(renamed)
    }

    companion object {
        const val init_auth_sqlite = "io/medatarun/auth/infra/db/init__auth_sqlite.sql"
        const val init_auth_postgresql = "io/medatarun/auth/infra/db/init__auth_postgresql.sql"
        const val v001_users = "io/medatarun/auth/infra/db/v001__auth_init_users_sqlite.sql"
        const val v001_oidc = "io/medatarun/auth/infra/db/v001__auth_init_oidc_sqlite.sql"
        const val v001_actors = "io/medatarun/auth/infra/db/v001__auth_init_actors_sqlite.sql"
        const val v002_ids_binary16_sqlite = "io/medatarun/auth/infra/db/v002__auth_ids_binary16_sqlite.sql"
        const val v002_auth_client_sqlite = "io/medatarun/auth/infra/db/v002__auth_client.sql"
    }
}
