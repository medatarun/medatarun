package io.medatarun.actions.infra.db

import io.medatarun.platform.db.DbDialect
import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbMigrationContext

class ActionAuditRecorderDbMigration(override val pluginId: String) : DbMigration {

    override fun install(ctx: DbMigrationContext) {
        when (ctx.dialect) {
            DbDialect.SQLITE -> ctx.applySqlResource(init_actions_sqlite)
            DbDialect.POSTGRESQL -> ctx.applySqlResource(init_actions_postgresql)
        }
    }

    override fun latestVersion(): Int {
        return 2
    }

    override fun applyVersion(version: Int, ctx: DbMigrationContext) {
        when (version) {
            1 -> ctx.applySqlResource(v001)
            2 -> ctx.applySqlResource(v002)
            else -> ctx.throwUnknownVersionException()
        }
    }

    companion object {
        const val init_actions_sqlite = "io/medatarun/actions/infra/db/init__actions_sqlite.sql"
        const val init_actions_postgresql = "io/medatarun/actions/infra/db/init__actions_postgresql.sql"
        const val v001 = "io/medatarun/actions/infra/db/v001__actions_init_db_sqlite.sql"
        const val v002 = "io/medatarun/actions/infra/db/v002__actions_ids_timestamps_sqlite.sql"
    }
}
