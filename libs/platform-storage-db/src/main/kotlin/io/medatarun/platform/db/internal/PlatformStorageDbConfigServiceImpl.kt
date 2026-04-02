package io.medatarun.platform.db.internal

import io.medatarun.platform.db.PlatformStorageDbConfigService
import io.medatarun.platform.kernel.MedatarunExtensionCtxConfig
import java.util.Properties

class PlatformStorageDbConfigServiceImpl(private val config: MedatarunExtensionCtxConfig) : PlatformStorageDbConfigService {
    override fun getJdbcUrl(): String? {
        return config.getConfigProperty(JDBC_URL_PROPERTY)
    }

    override fun getJdbcProperties(): Properties {
        val properties = Properties()
        val propertyNames = config.getConfigPropertyNamesStartingWith(JDBC_PROPERTIES_PREFIX)
        for (propertyName in propertyNames) {
            val propertyValue = config.getConfigProperty(propertyName)
            val propertyKey = propertyName.removePrefix(JDBC_PROPERTIES_PREFIX)
            val propertyValueIsUsable = !propertyValue.isNullOrBlank()
            if (propertyValueIsUsable && propertyKey.isNotBlank()) {
                properties.setProperty(propertyKey, propertyValue)
            }
        }
        return properties
    }

    override fun getDbEngine(): String? {
        return config.getConfigProperty(DB_ENGINE_PROPERTY)
    }

    companion object {
        const val DB_ENGINE_PROPERTY = "medatarun.storage.datasource.jdbc.dbengine"
        const val JDBC_URL_PROPERTY = "medatarun.storage.datasource.jdbc.url"
        const val JDBC_PROPERTIES_PREFIX = "medatarun.storage.datasource.jdbc.properties."
    }
}
