package io.medatarun.model.infra.db

import io.medatarun.model.infra.db.migrations.V002_ModelFixTypeEvents
import io.medatarun.model.infra.db.migrations.V003_IdentifierAttributeToPrimaryKeys
import io.medatarun.platform.db.DbDialect
import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbMigrationContext

class ModelStorageDbMigration(override val pluginId: String) : DbMigration {
    private val v002ModelFixTypeEvents = V002_ModelFixTypeEvents()
    private val v003IdentifierAttributeToPrimaryKeys = V003_IdentifierAttributeToPrimaryKeys()

    override fun install(ctx: DbMigrationContext) {
        when (ctx.dialect) {
            DbDialect.SQLITE -> ctx.applySqlResource(init_models_sqlite)
            DbDialect.POSTGRESQL -> ctx.applySqlResource(init_models_postgresql)
        }
    }

    override fun latestVersion(): Int {
        return 3
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
            // v0.10.0 (current)
            3 -> {
                when (ctx.dialect) {
                    DbDialect.SQLITE -> ctx.applySqlResource(v003_01_pk_bk_sqlite)
                    DbDialect.POSTGRESQL -> ctx.applySqlResource(v003_01_pk_bk_postgresql)
                }
                v003IdentifierAttributeToPrimaryKeys.migrate(ctx)
            }
            else -> ctx.throwUnknownVersionException()
        }
    }

    companion object {
        const val init_models_sqlite = "io/medatarun/model/infra/db/init__models_sqlite.sql"
        const val init_models_postgresql = "io/medatarun/model/infra/db/init__models_postgresql.sql"
        const val v001 = "io/medatarun/model/infra/db/version_models_v001_init_db_sqlite.sql"
        const val v002_traceability = "io/medatarun/model/infra/db/version_models_v002_01_models_upgrade_traceability.sql"
        const val v002_uids_binary = "io/medatarun/model/infra/db/version_models_v002_02_ids_binary16_sqlite.sql"
        const val v003_01_pk_bk_sqlite = "io/medatarun/model/infra/db/version_models_v003_01_pk_bk_sqlite.sql"
        const val v003_01_pk_bk_postgresql = "io/medatarun/model/infra/db/version_models_v003_01_pk_bk_postgresql.sql"
    }
}
