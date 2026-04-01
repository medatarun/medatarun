package io.medatarun.actions.infra.db

import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbMigrationContext

class ActionAuditRecorderDbMigration(override val pluginId: String) : DbMigration {

    override fun install(ctx: DbMigrationContext) {
        ctx.applySqlResource(init_actions_sqlite)
    }

    override fun latestVersion(): Int {
        return 1
    }

    override fun applyVersion(version: Int, ctx: DbMigrationContext) {
        when (version) {
            1 -> ctx.applySqlResource(v001)
            else -> ctx.throwUnknownVersionException()
        }
    }

    companion object {
        const val init_actions_sqlite = "io/medatarun/actions/infra/db/init__actions.sql"
        const val v001 = "io/medatarun/actions/infra/db/v001__actions_init_db_sqlite.sql"
    }
}
