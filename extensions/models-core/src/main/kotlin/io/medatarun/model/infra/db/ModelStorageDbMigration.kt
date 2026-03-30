package io.medatarun.model.infra.db

import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbMigrationContext

class ModelStorageDbMigration(override val pluginId: String) : DbMigration {

    override fun install(ctx: DbMigrationContext) {
        ctx.applySqlResource(v001)
        ctx.applySqlResource(v002_traceability)
        ctx.applySqlResource(v003_uids_binary)
    }

    override fun latestVersion(): Int {
        return 2
    }

    override fun applyVersion(version: Int, ctx: DbMigrationContext) {
        when (version) {
            1 -> ctx.applySqlResource(v001)
            2 -> {
                ctx.applySqlResource(v002_traceability)
                ctx.applySqlResource(v003_uids_binary)
            }
            else -> ctx.throwUnknownVersionException()
        }
    }

    companion object {
        const val v001 = "io/medatarun/model/infra/db/v001__models_init_db_sqlite.sql"
        const val v002_traceability = "io/medatarun/model/infra/db/v002__models_upgrade_traceability.sql"
        const val v003_uids_binary = "io/medatarun/model/infra/db/v002__models_ids_binary16_sqlite.sql"
    }
}
