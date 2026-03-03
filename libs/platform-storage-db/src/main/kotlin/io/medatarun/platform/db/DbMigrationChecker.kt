package io.medatarun.platform.db

/**
 * Tool to test migrations happened, mostly for extensions unit tests
 */
interface DbMigrationChecker {
    /**
     * @return true if table exists
     */
    fun tableExists(tableName: String): Boolean
    /**
     * @return migration counts for this plugin
     */
    fun migrationCount(pluginId: String): Int
    /**
     * @return current stored version for this plugin
     */
    fun currentVersion(pluginId: String): Int
}