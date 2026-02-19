package io.medatarun.platform.db.sqlite

import io.medatarun.platform.db.DbProvider
import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx

class PlatformStorageDbSqliteExtension : MedatarunExtension {
    override val id: ExtensionId = "platform-storage-db-sqlite"
    override fun init(ctx: MedatarunExtensionCtx) {
        val dbConnectionFactory = DbProviderSqlite(
            ctx.resolveApplicationHomePath("data/database.db").toAbsolutePath().toString()
        )
        ctx.register(DbProvider::class, dbConnectionFactory)
    }
    override fun initServices(ctx: MedatarunServiceCtx) {
    }
}