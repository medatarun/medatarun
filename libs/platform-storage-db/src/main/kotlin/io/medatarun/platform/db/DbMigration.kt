package io.medatarun.platform.db

import java.sql.Connection

/**
 * Declares the schema evolution owned by one extension.
 *
 * The runner creates one [DbMigrationContext] per plugin and lets the plugin apply whatever versions
 * are still missing from the current database state.
 */
interface DbMigration {
    val pluginId: String
    fun install(ctx: DbMigrationContext)
    fun latestVersion(): Int
    fun applyVersion(version: Int, ctx: DbMigrationContext)
}

/**
 * Runtime context available while one plugin applies its pending schema versions.
 *
 * [currentVersion] is the effective version already applied for the current plugin in the target database.
 * For a fresh install, [currentVersion] is `0` and the runner calls [DbMigration.install].
 * For an upgrade, the runner calls [DbMigration.applyVersion] one version at a time until
 * [DbMigration.latestVersion].
 */
interface DbMigrationContext {
    val pluginId: String
    val currentVersion: Int
    val dialect: DbDialect

    fun <T> withConnection(block: (Connection) -> T): T
    fun applySqlResource(resourcePath: String)
    fun throwUnknownVersionException()
}

enum class DbDialect {
    SQLITE,
    POSTGRESQL,
    MYSQL,
    ORACLE
}
