package io.medatarun.platform.db.testkit

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.PlatformStorageDbConfigProperty
import io.medatarun.platform.db.sqlite.DbProviderSqlite

/**
 * Centralized DB test properties for test environments.
 *
 * It resolves the configured DB engine from JVM properties first, then from
 * environment variables using MicroProfile naming style:
 * medatarun.storage.datasource.jdbc.dbengine -> MEDATARUN_STORAGE_DATASOURCE_JDBC_DBENGINE
 */
object TestDbConfig {
    fun testDatabaseProperties(): Map<String, String> {
        val dbEngineConfig = getConfigProperty(PlatformStorageDbConfigProperty.DbEngine.key)
        if (dbEngineConfig != null && dbEngineConfig.equals(DB_ENGINE_POSTGRESQL, ignoreCase = true)) {
            return postgresqlTestDatabaseProperties()
        } else {
            return sqliteTestDatabaseProperties()
        }
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

    private const val DB_ENGINE_POSTGRESQL = "postgresql"

    private fun sqliteTestDatabaseProperties(): Map<String, String> {
        return mapOf(
            PlatformStorageDbConfigProperty.JdbcUrl.key to DbProviderSqlite.randomDbUrl()
        )
    }

    private fun postgresqlTestDatabaseProperties(): Map<String, String> {
        throw PostgresqlTestDbConfigNotImplementedException()
    }
}

class PostgresqlTestDbConfigNotImplementedException :
    MedatarunException("PostgreSQL test database properties are not implemented yet in TestDbConfig.")
