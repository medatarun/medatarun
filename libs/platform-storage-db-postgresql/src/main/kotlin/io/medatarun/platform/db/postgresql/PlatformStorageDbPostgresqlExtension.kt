package io.medatarun.platform.db.postgresql

import io.medatarun.platform.db.DbProvider
import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import java.util.Properties

class PlatformStorageDbPostgresqlExtension : MedatarunExtension {
    override val id: ExtensionId = "platform-storage-db-postgresql"

    override fun initContributions(ctx: MedatarunExtensionCtx) {
        val dbEngine = ctx.getConfigProperty(DB_ENGINE_PROPERTY)
        val dbEngineIsPostgresql = dbEngine != null && dbEngine.equals(DB_ENGINE_POSTGRESQL, ignoreCase = true)
        val url = ctx.getConfigProperty(JDBC_URL_PROPERTY)
        if (dbEngineIsPostgresql && url != null) {
            val properties = Properties()
            val propertyNames = ctx.getConfigPropertyNamesStartingWith(JDBC_PROPERTIES_PREFIX)
            for (propertyName in propertyNames) {
                val propertyValue = ctx.getConfigProperty(propertyName)
                val propertyKey = propertyName.removePrefix(JDBC_PROPERTIES_PREFIX)
                val propertyValueIsUsable = propertyValue != null && propertyValue.isNotBlank()
                if (propertyValueIsUsable && propertyKey.isNotBlank()) {
                    properties.setProperty(propertyKey, propertyValue)
                }
            }
            val dbProvider = DbProviderPostgresql(url, properties)
            ctx.registerContribution(DbProvider::class, dbProvider)
        }
    }

    override fun initServices(ctx: MedatarunServiceCtx) {
    }

    companion object {
        const val DB_ENGINE_PROPERTY = "medatarun.storage.datasource.jdbc.dbengine"
        const val DB_ENGINE_POSTGRESQL = "postgresql"
        const val JDBC_URL_PROPERTY = "medatarun.storage.datasource.jdbc.url"
        const val JDBC_PROPERTIES_PREFIX = "medatarun.storage.datasource.jdbc.properties."
    }
}
