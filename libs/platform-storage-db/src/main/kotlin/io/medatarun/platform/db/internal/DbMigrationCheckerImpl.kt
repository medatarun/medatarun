package io.medatarun.platform.db.internal

import io.medatarun.platform.db.DbMigrationChecker
import io.medatarun.platform.db.adapters.DbConnectionFactoryImpl

class DbMigrationCheckerImpl(val connectionFactory: DbConnectionFactoryImpl) : DbMigrationChecker {

    override fun tableExists(tableName: String): Boolean {
        return connectionFactory.withConnection { connection ->
            connection.prepareStatement(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = ?"
            ).use { statement ->
                statement.setString(1, tableName)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getInt(1) == 1
                }
            }
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

}
