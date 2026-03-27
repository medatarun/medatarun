package io.medatarun.tags.core.infra.db

import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbMigrationContext

class TagsCoreDbMigration(override val pluginId: String) : DbMigration {


    override fun install(ctx: DbMigrationContext) {
        ctx.applySqlResource(v001)
        ctx.applySqlResource(v002_base)
        ctx.applySqlResource(v002_history)
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
            }
            else -> ctx.throwUnknownVersionException()
        }
    }

    companion object {
        const val v001 = "io/medatarun/tags/core/infra/db/v001__tags_init_sqlite.sql"
        const val v002_base = "io/medatarun/tags/core/infra/db/v002__tags_events_and_projection_sqlite.sql"
        const val v002_history = "io/medatarun/tags/core/infra/db/v002__tags_history_projection_sqlite.sql"
    }


}
