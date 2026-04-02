package io.medatarun.platform.db.testkit

import io.medatarun.platform.db.PlatformStorageDbConfigProperty
import io.medatarun.platform.db.sqlite.DbProviderSqlite
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.sql.DriverManager

/**
 * Manages test database lifecycle for the full JUnit test plan.
 *
 * The extension is the single source of truth for database test properties:
 * - decides sqlite or postgresql from config/environment
 * - writes the effective JDBC properties into JVM system properties
 * - recreates state before each test
 */
class EnableDatabaseTestsExtension : BeforeAllCallback, BeforeEachCallback {
    override fun beforeAll(context: ExtensionContext) {
        getOrCreateResource(context)
    }

    override fun beforeEach(context: ExtensionContext) {
        val resource = getOrCreateResource(context)
        resource.prepareBeforeEachTest()
    }

    private fun getOrCreateResource(context: ExtensionContext): DatabaseLifecycleResource {
        val store = context.root.getStore(STORE_NAMESPACE)
        val existingResource = store.get(STORE_KEY, DatabaseLifecycleResource::class.java)
        if (existingResource != null) {
            return existingResource
        }
        val createdResource = DatabaseLifecycleResource()
        store.put(STORE_KEY, createdResource)
        return createdResource
    }

    private class DatabaseLifecycleResource : ExtensionContext.Store.CloseableResource {
        private val dbEngine: String = resolveConfiguredDbEngine()
        private var container: PostgreSQLContainer? = null
        private val originalLifecycleMarker: String? = System.getProperty(TESTKIT_LIFECYCLE_MARKER_KEY)
        private val originalDbEngine: String? = System.getProperty(PlatformStorageDbConfigProperty.DbEngine.key)
        private val originalJdbcUrl: String? = System.getProperty(PlatformStorageDbConfigProperty.JdbcUrl.key)
        private val originalUser: String? = System.getProperty(JDBC_USER_KEY)
        private val originalPassword: String? = System.getProperty(JDBC_PASSWORD_KEY)

        init {
            System.setProperty(TESTKIT_LIFECYCLE_MARKER_KEY, TESTKIT_LIFECYCLE_MARKER_ACTIVE_VALUE)
            if (isPostgresqlEngine(dbEngine)) {
                startPostgresqlContainer()
            } else {
                applySqliteSystemProperties()
            }
        }

        fun prepareBeforeEachTest() {
            if (isPostgresqlEngine(dbEngine)) {
                recreatePostgresqlSchema()
            } else {
                System.setProperty(PlatformStorageDbConfigProperty.JdbcUrl.key, DbProviderSqlite.randomDbUrl())
            }
        }

        private fun startPostgresqlContainer() {
            val createdContainer = PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE))
            createdContainer.start()
            container = createdContainer
            applyPostgresqlSystemProperties(createdContainer)
            recreatePostgresqlSchema()
        }

        private fun recreatePostgresqlSchema() {
            val activeContainer = container ?: return
            val sql =
                "DROP SCHEMA IF EXISTS $SCHEMA_NAME CASCADE; CREATE SCHEMA $SCHEMA_NAME AUTHORIZATION ${activeContainer.username};"
            DriverManager.getConnection(activeContainer.jdbcUrl, activeContainer.username, activeContainer.password)
                .use { connection ->
                connection.createStatement().use { statement ->
                    statement.execute(sql)
                }
            }
        }

        private fun applySqliteSystemProperties() {
            System.setProperty(PlatformStorageDbConfigProperty.DbEngine.key, DB_ENGINE_SQLITE)
            System.setProperty(PlatformStorageDbConfigProperty.JdbcUrl.key, DbProviderSqlite.randomDbUrl())
            System.clearProperty(JDBC_USER_KEY)
            System.clearProperty(JDBC_PASSWORD_KEY)
        }

        private fun applyPostgresqlSystemProperties(activeContainer: PostgreSQLContainer) {
            System.setProperty(PlatformStorageDbConfigProperty.DbEngine.key, DB_ENGINE_POSTGRESQL)
            System.setProperty(
                PlatformStorageDbConfigProperty.JdbcUrl.key,
                withQueryParameter(activeContainer.jdbcUrl, CURRENT_SCHEMA_PARAM, SCHEMA_NAME)
            )
            System.setProperty(JDBC_USER_KEY, activeContainer.username)
            System.setProperty(JDBC_PASSWORD_KEY, activeContainer.password)
        }

        override fun close() {
            restoreSystemProperty(TESTKIT_LIFECYCLE_MARKER_KEY, originalLifecycleMarker)
            restoreSystemProperty(PlatformStorageDbConfigProperty.DbEngine.key, originalDbEngine)
            restoreSystemProperty(PlatformStorageDbConfigProperty.JdbcUrl.key, originalJdbcUrl)
            restoreSystemProperty(JDBC_USER_KEY, originalUser)
            restoreSystemProperty(JDBC_PASSWORD_KEY, originalPassword)
            val activeContainer = container
            if (activeContainer != null) {
                activeContainer.stop()
                container = null
            }
        }

        private fun restoreSystemProperty(key: String, value: String?) {
            if (value == null) {
                System.clearProperty(key)
            } else {
                System.setProperty(key, value)
            }
        }
    }

    companion object {
        private val STORE_NAMESPACE =
            ExtensionContext.Namespace.create(EnableDatabaseTestsExtension::class.java)
        private const val STORE_KEY = "postgresql-test-container-resource"
        private const val POSTGRES_IMAGE = "postgres:17-alpine"
        private const val SCHEMA_NAME = "medatarun_test"
        private const val CURRENT_SCHEMA_PARAM = "currentSchema"
        private const val DB_ENGINE_POSTGRESQL = "postgresql"
        private const val DB_ENGINE_SQLITE = "sqlite"
        private const val TESTKIT_LIFECYCLE_MARKER_ACTIVE_VALUE = "active"
        const val TESTKIT_LIFECYCLE_MARKER_KEY = "medatarun.testkit.database.lifecycle"
        private val JDBC_PROPERTIES_PREFIX = PlatformStorageDbConfigProperty.JdbcPropertiesEntry.prefixKey()
        private val JDBC_USER_KEY = "${JDBC_PROPERTIES_PREFIX}user"
        private val JDBC_PASSWORD_KEY = "${JDBC_PROPERTIES_PREFIX}password"

        private fun resolveConfiguredDbEngine(): String {
            val configured = getConfigProperty(PlatformStorageDbConfigProperty.DbEngine.key)
            if (configured == null) {
                return DB_ENGINE_SQLITE
            }
            if (isPostgresqlEngine(configured)) {
                return DB_ENGINE_POSTGRESQL
            }
            return DB_ENGINE_SQLITE
        }

        private fun isPostgresqlEngine(value: String): Boolean {
            return value.equals(DB_ENGINE_POSTGRESQL, ignoreCase = true)
        }

        private fun getConfigProperty(key: String): String? {
            val systemPropertyValue = System.getProperty(key)
            if (systemPropertyValue != null) {
                return systemPropertyValue
            }
            val envKey = key.replace(".", "_").uppercase()
            return System.getenv(envKey)
        }

        private fun withQueryParameter(url: String, parameter: String, value: String): String {
            val separator = if (url.contains("?")) "&" else "?"
            return "$url$separator$parameter=$value"
        }
    }
}
