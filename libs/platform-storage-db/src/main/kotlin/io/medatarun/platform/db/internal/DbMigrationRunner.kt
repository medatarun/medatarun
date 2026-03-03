package io.medatarun.platform.db.internal

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.*
import io.medatarun.platform.kernel.ExtensionRegistry
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.time.Instant

/**
 * Applies versioned schema migrations contributed by extensions.
 *
 * The runner keeps one history row per plugin version so the current state of each plugin schema can be
 * read directly from the database.
 */
class DbMigrationRunner(
    private val extensionRegistry: ExtensionRegistry,
    private val dbConnectionFactory: DbConnectionFactory,
    private val dbTransactionManager: DbTransactionManager
) {
    fun runAll() {
        val migrations = extensionRegistry.findContributionsFlat(DbMigration::class)
        ensureHistoryTable()
        migrations.forEach { migration ->
            val currentVersion = dbConnectionFactory.withConnection { connection ->
                readCurrentVersion(connection, migration.pluginId)
            }
            if (currentVersion == 0) {
                installMigration(migration)
            } else {
                applyPendingVersions(migration, currentVersion)
            }
        }
    }

    private fun ensureHistoryTable() {
        dbConnectionFactory.withConnection { connection ->
            connection.createStatement().use { statement ->
                statement.execute(SCHEMA_VERSION_HISTORY_TABLE_SQL)
            }
        }
    }

    private fun readCurrentVersion(connection: Connection, pluginId: String): Int {
        connection.prepareStatement(
            "SELECT MAX(version) FROM $SCHEMA_VERSION_HISTORY_TABLE_NAME WHERE plugin_id = ?"
        ).use { statement ->
            statement.setString(1, pluginId)
            statement.executeQuery().use { resultSet ->
                resultSet.next()
                return resultSet.getInt(1)
            }
        }
    }

    private fun recordAppliedVersion(
        connection: Connection,
        pluginId: String,
        version: Int
    ) {
        connection.prepareStatement(
            """
INSERT INTO $SCHEMA_VERSION_HISTORY_TABLE_NAME(plugin_id, version, applied_at)
VALUES (?, ?, ?)
"""
        ).use { statement ->
            statement.setString(1, pluginId)
            statement.setInt(2, version)
            statement.setString(3, Instant.now().toString())
            statement.executeUpdate()
        }
    }

    private fun applyPendingVersions(migration: DbMigration, currentVersion: Int) {
        val latestVersion = migration.latestVersion()
        if (latestVersion < currentVersion) {
            throw DbMigrationLatestVersionTooLowException(migration.pluginId, currentVersion, latestVersion)
        }
        var effectiveVersion = currentVersion
        var versionToApply = currentVersion + 1
        while (versionToApply <= latestVersion) {
            applyVersion(migration, effectiveVersion, versionToApply)
            effectiveVersion = versionToApply
            versionToApply += 1
        }
    }

    private fun installMigration(migration: DbMigration) {
        val latestVersion = migration.latestVersion()
        if (latestVersion == 0) {
            return
        }
        dbTransactionManager.runInTransaction {
            val context = DbMigrationContextImpl(
                pluginId = migration.pluginId,
                currentVersion = 0,
                dbConnectionFactory = dbConnectionFactory
            )
            logger.info("Installing database schema for {} up to version {}", migration.pluginId, latestVersion)
            migration.install(context)
            dbConnectionFactory.withConnection { connection ->
                recordAppliedVersion(connection, migration.pluginId, latestVersion)
            }
        }
    }

    private fun applyVersion(migration: DbMigration, currentVersion: Int, versionToApply: Int) {
        dbTransactionManager.runInTransaction {
            val context = DbMigrationContextImpl(
                pluginId = migration.pluginId,
                currentVersion = currentVersion,
                dbConnectionFactory = dbConnectionFactory
            )
            logger.info("Applying database migration {} version {}", migration.pluginId, versionToApply)
            migration.applyVersion(versionToApply, context)
            dbConnectionFactory.withConnection { connection ->
                recordAppliedVersion(connection, migration.pluginId, versionToApply)
            }
        }
    }

    private inner class DbMigrationContextImpl(
        override val pluginId: String,
        override val currentVersion: Int,
        private val dbConnectionFactory: DbConnectionFactory
    ) : DbMigrationContext {
        override val dialect: DbDialect = dbConnectionFactory.withConnection { connection ->
            resolveDialect(connection)
        }

        override fun <T> withConnection(block: (Connection) -> T): T {
            return dbConnectionFactory.withConnection(block)
        }

        override fun applySqlResource(resourcePath: String) {
            DbSqlResources.executeClasspathResource(dbConnectionFactory, resourcePath)
        }

        override fun throwUnknownVersionException() {
            throw DbMigrationRunnerUnknownVersionException(pluginId, currentVersion)
        }
    }

    private fun resolveDialect(connection: Connection): DbDialect {
        val productName = connection.metaData.databaseProductName
        return when (productName.lowercase()) {
            "sqlite" -> DbDialect.SQLITE
            "postgresql" -> DbDialect.POSTGRESQL
            "mysql" -> DbDialect.MYSQL
            "oracle" -> DbDialect.ORACLE
            else -> throw DbMigrationUnknownDialectException(productName)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DbMigrationRunner::class.java)
        const val SCHEMA_VERSION_HISTORY_TABLE_NAME = "schema_version_history"
        private const val SCHEMA_VERSION_HISTORY_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS $SCHEMA_VERSION_HISTORY_TABLE_NAME (
  plugin_id TEXT NOT NULL,
  version INTEGER NOT NULL,
  applied_at TEXT NOT NULL,
  PRIMARY KEY (plugin_id, version)
)
"""
    }

    class DbMigrationLatestVersionTooLowException(
        pluginId: String,
        currentVersion: Int,
        latestVersion: Int
    ) : MedatarunException(
        "Migration plugin [$pluginId] declares latest version [$latestVersion] but database is already at version [$currentVersion]"
    )


}