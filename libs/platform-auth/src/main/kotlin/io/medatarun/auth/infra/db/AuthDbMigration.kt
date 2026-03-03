package io.medatarun.auth.infra.db

import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbMigrationContext

class AuthDbMigration : DbMigration {
    override val pluginId: String = "platform-auth"

    override fun install(ctx: DbMigrationContext) {
        ctx.applySqlResource(v000__init_users_sqlite)
        ctx.applySqlResource(v000__init_oidc_sqlite)
        ctx.applySqlResource(v000__init_actors_sqlite)
    }

    override fun latestVersion(): Int {
        return 1
    }

    override fun applyVersion(version: Int, ctx: DbMigrationContext) {
        when (version) {
            1 -> ctx.throwUnknownVersionException()
            else -> ctx.throwUnknownVersionException()
        }
    }

    companion object {
        const val v000__init_users_sqlite = "io/medatarun/auth/infra/db/v000__init_users_sqlite.sql"
        const val v000__init_oidc_sqlite = "io/medatarun/auth/infra/db/v000__init_oidc_sqlite.sql"
        const val v000__init_actors_sqlite = "io/medatarun/auth/infra/db/v000__init_actors_sqlite.sql"
    }
}