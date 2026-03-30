package io.medatarun.model.infra.db

import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbMigrationContext

class ModelStorageDbMigration(override val pluginId: String) : DbMigration {
    private val v002ModelFixTypeEvents = V002_ModelFixTypeEvents()

    override fun install(ctx: DbMigrationContext) {
        ctx.applySqlResource(v001)
        ctx.applySqlResource(v002_traceability)
        ctx.applySqlResource(v002_uids_binary)
    }

    override fun latestVersion(): Int {
        return 2
    }

    override fun applyVersion(version: Int, ctx: DbMigrationContext) {
        when (version) {
            // v0.8.0
            1 -> ctx.applySqlResource(v001)
            // v0.9.0 (current)
            2 -> {
                ctx.applySqlResource(v002_traceability)
                ctx.applySqlResource(v002_uids_binary)
                v002ModelFixTypeEvents.migrate(ctx)
            }
            else -> ctx.throwUnknownVersionException()
        }
    }

    companion object {
        const val v001 = "io/medatarun/model/infra/db/v001__models_init_db_sqlite.sql"
        const val v002_traceability = "io/medatarun/model/infra/db/v002__models_upgrade_traceability.sql"
        const val v002_uids_binary = "io/medatarun/model/infra/db/v002__models_ids_binary16_sqlite.sql"
    }
}
