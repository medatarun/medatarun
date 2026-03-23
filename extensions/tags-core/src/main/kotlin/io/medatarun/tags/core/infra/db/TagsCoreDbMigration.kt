package io.medatarun.tags.core.infra.db

import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbMigrationContext

class TagsCoreDbMigration(override val pluginId: String) : DbMigration {


    override fun install(ctx: DbMigrationContext) {
        ctx.applySqlResource(v000_init_tags_sqlite)
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
        const val v000_init_tags_sqlite = "io/medatarun/tags/core/infra/db/v000__init_tags_sqlite.sql"
    }


}