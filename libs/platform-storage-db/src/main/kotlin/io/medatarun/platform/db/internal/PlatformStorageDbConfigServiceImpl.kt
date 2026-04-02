package io.medatarun.platform.db.internal

import io.medatarun.platform.db.PlatformStorageDbConfigService
import io.medatarun.platform.db.PlatformStorageDbConfigProperty
import io.medatarun.platform.kernel.MedatarunExtensionCtxConfig
import java.util.Properties

class PlatformStorageDbConfigServiceImpl(private val config: MedatarunExtensionCtxConfig) : PlatformStorageDbConfigService {
    override fun getJdbcUrl(): String? {
        return config.getConfigProperty(PlatformStorageDbConfigProperty.JdbcUrl.key)
    }

    override fun getJdbcProperties(): Properties {
        val properties = Properties()
        val jdbcPropertiesPrefix = PlatformStorageDbConfigProperty.JdbcPropertiesEntry.prefixKey()
        val propertyMap = config.getConfigPropertyMapStartingWith(jdbcPropertiesPrefix)
        for (entry in propertyMap.entries) {
            val propertyKey = entry.key
            val propertyValue = entry.value
            if (propertyKey.isNotBlank() && propertyValue.isNotBlank()) {
                properties.setProperty(propertyKey, propertyValue)
            }
        }
        return properties
    }

    override fun getDbEngine(): String? {
        return config.getConfigProperty(PlatformStorageDbConfigProperty.DbEngine.key)
    }
}
