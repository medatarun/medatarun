package io.medatarun.platform.db.testkit

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.PlatformStorageDbConfigProperty

/**
 * Centralized DB test properties for test environments.
 *
 * This class only relays properties already prepared by the database lifecycle extension.
 */
class TestDbConfig {
    init {
        requireDatabaseLifecycleExtensionIsActive()
    }

    fun testDatabaseProperties(): Map<String, String> {
        val dbEngine = requiredConfigProperty(PlatformStorageDbConfigProperty.DbEngine.key)
        val jdbcUrl = requiredConfigProperty(PlatformStorageDbConfigProperty.JdbcUrl.key)
        val jdbcPropertiesPrefix = PlatformStorageDbConfigProperty.JdbcPropertiesEntry.prefixKey()
        val properties = LinkedHashMap<String, String>()
        properties[PlatformStorageDbConfigProperty.DbEngine.key] = dbEngine
        properties[PlatformStorageDbConfigProperty.JdbcUrl.key] = jdbcUrl
        if (dbEngine.equals(DB_ENGINE_POSTGRESQL, ignoreCase = true)) {
            properties["${jdbcPropertiesPrefix}user"] = requiredConfigProperty("${jdbcPropertiesPrefix}user")
            properties["${jdbcPropertiesPrefix}password"] = requiredConfigProperty("${jdbcPropertiesPrefix}password")
        }
        return properties
    }

    fun testDatabaseProperties(extraProps: Map<String, String>): Map<String, String> {
        val props = LinkedHashMap<String, String>()
        props.putAll(testDatabaseProperties())
        props.putAll(extraProps)
        return props
    }

    /**
     * Returns the configured value for [key] from system properties first,
     * then from environment variables.
     */
    fun getConfigProperty(key: String): String? {
        val systemPropertyValue = System.getProperty(key)
        if (systemPropertyValue != null) {
            return systemPropertyValue
        }
        val envKey = key.replace(".", "_").uppercase()
        return System.getenv(envKey)
    }

    private fun requireDatabaseLifecycleExtensionIsActive() {
        val lifecycleMarker =
            System.getProperty(EnableDatabaseTestsExtension.TESTKIT_LIFECYCLE_MARKER_KEY)
        if (lifecycleMarker == TESTKIT_LIFECYCLE_MARKER_ACTIVE_VALUE) {
            return
        }
        throw DatabaseLifecycleExtensionNotActiveInTestDbConfigException()
    }

    private fun requiredConfigProperty(key: String): String {
        val value = getConfigProperty(key)
        if (value != null) {
            return value
        }
        throw RequiredDatabasePropertyMissingInTestDbConfigException(key)
    }

    companion object {
        private const val DB_ENGINE_POSTGRESQL = "postgresql"
        private const val TESTKIT_LIFECYCLE_MARKER_ACTIVE_VALUE = "active"
    }
}

class DatabaseLifecycleExtensionNotActiveInTestDbConfigException :
    MedatarunException(
        "TestDbConfig requires @EnableDatabaseTests so the lifecycle extension can prepare DB properties."
    )

class RequiredDatabasePropertyMissingInTestDbConfigException(key: String) :
    MedatarunException(
        "TestDbConfig requires property '$key' to be prepared by the database lifecycle extension."
    )
