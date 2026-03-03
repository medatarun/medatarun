package io.medatarun.model.infra.db

import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbMigrationContext

class ModelStorageDbMigration(override val pluginId: String) : DbMigration {

    override fun install(ctx: DbMigrationContext) {
        ctx.applySqlResource(V000_INIT_DB_SQLITE)
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
        const val V000_INIT_DB_SQLITE = "io/medatarun/model/infra/db/v000_init_db_sqlite.sql"
    }
}
