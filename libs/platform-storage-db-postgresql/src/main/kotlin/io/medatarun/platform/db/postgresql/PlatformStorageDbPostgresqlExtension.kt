package io.medatarun.platform.db.postgresql

import io.medatarun.platform.db.DbProvider
import io.medatarun.platform.db.PlatformStorageDbConfigService
import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx

class PlatformStorageDbPostgresqlExtension : MedatarunExtension {
    override val id: ExtensionId = "platform-storage-db-postgresql"

    override fun initContributions(ctx: MedatarunExtensionCtx) {
        val configService = ctx.getService(PlatformStorageDbConfigService::class)
        val dbEngine = configService.getDbEngine()
        val dbEngineIsPostgresql = dbEngine != null && dbEngine.equals(DB_ENGINE_POSTGRESQL, ignoreCase = true)
        val url = configService.getJdbcUrl()
        if (dbEngineIsPostgresql && url != null) {
            val properties = configService.getJdbcProperties()
            val dbProvider = DbProviderPostgresql(url, properties)
            ctx.registerContribution(DbProvider::class, dbProvider)
        }
    }

    override fun initServices(ctx: MedatarunServiceCtx) {
    }

    companion object {
        const val DB_ENGINE_POSTGRESQL = "postgresql"
    }
}
