package io.medatarun.platform.db.internal

import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbMigrationChecker
import java.sql.Connection

class DbMigrationCheckerImpl(private val connectionFactory: DbConnectionFactory) : DbMigrationChecker {

    override fun tableExists(tableName: String): Boolean {
        return connectionFactory.withConnection { connection ->
            tableExists(connection, tableName)
        }
    }

    override fun migrationCount(pluginId: String): Int {
        return connectionFactory.withConnection { connection ->
            connection.prepareStatement(
                "SELECT COUNT(*) FROM ${DbMigrationRunner.SCHEMA_VERSION_HISTORY_TABLE_NAME} WHERE plugin_id = ?"
            ).use { statement ->
                statement.setString(1, pluginId)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getInt(1)
                }
            }
        }
    }

    override fun currentVersion(pluginId: String): Int {
        return connectionFactory.withConnection { connection ->
            connection.prepareStatement(
                "SELECT MAX(version) FROM ${DbMigrationRunner.SCHEMA_VERSION_HISTORY_TABLE_NAME} WHERE plugin_id = ?"
            ).use { statement ->
                statement.setString(1, pluginId)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getInt(1)
                }
            }
        }
    }

    /**
     * JDBC metadata is the portable way to check table presence across SQLite, PostgreSQL and the next
     * supported vendors. Different drivers normalize identifiers differently, so we probe the original,
     * lower-case and upper-case names before declaring the table missing.
     */
    private fun tableExists(connection: Connection, tableName: String): Boolean {
        val candidateNames = listOf(tableName, tableName.lowercase(), tableName.uppercase()).distinct()
        val metadata = connection.metaData
        val tableTypes = arrayOf("TABLE")

        candidateNames.forEach { candidateName ->
            metadata.getTables(null, null, candidateName, tableTypes).use { resultSet ->
                if (resultSet.next()) {
                    return true
                }
            }
        }

        return false
    }
}
