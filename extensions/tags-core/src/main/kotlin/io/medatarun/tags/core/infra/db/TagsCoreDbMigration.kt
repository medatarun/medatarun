package io.medatarun.tags.core.infra.db

import io.medatarun.platform.db.DbDialect
import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbMigrationContext
import io.medatarun.security.AppActorId

class TagsCoreDbMigration(
    override val pluginId: String,
    private val maintenanceActorId: AppActorId
) : DbMigration {


    override fun install(ctx: DbMigrationContext) {
        when (ctx.dialect) {
            DbDialect.SQLITE -> ctx.applySqlResource(init_tags_sqlite)
            DbDialect.POSTGRESQL -> ctx.applySqlResource(init_tags_postgresql)
        }
    }

    override fun latestVersion(): Int {
        return 2
    }

    override fun applyVersion(version: Int, ctx: DbMigrationContext) {
        when (version) {
            1 -> ctx.applySqlResource(v001)
            2 -> {
                ctx.applySqlResource(v002_base)
                ctx.applySqlResource(v002_history)
                V002TagEventMigration(maintenanceActorId).migrate(ctx)
                ctx.applySqlResource(v002_drop_legacy)
            }
            else -> ctx.throwUnknownVersionException()
        }
    }

    companion object {
        const val init_tags_sqlite = "io/medatarun/tags/core/infra/db/init__tags_sqlite.sql"
        const val init_tags_postgresql = "io/medatarun/tags/core/infra/db/init__tags_postgresql.sql"
        const val v001 = "io/medatarun/tags/core/infra/db/v001__tags_init_sqlite.sql"
        const val v002_base = "io/medatarun/tags/core/infra/db/v002__tags_events_and_projection_sqlite.sql"
        const val v002_history = "io/medatarun/tags/core/infra/db/v002__tags_history_projection_sqlite.sql"
        const val v002_drop_legacy = "io/medatarun/tags/core/infra/db/v002__tags_drop_legacy_sqlite.sql"
    }


}
