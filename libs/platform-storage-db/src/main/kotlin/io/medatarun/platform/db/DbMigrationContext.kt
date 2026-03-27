package io.medatarun.platform.db

import java.sql.Connection

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
    fun <T> withExposed(block: () -> T): T
    fun applySqlResource(resourcePath: String)
    fun throwUnknownVersionException()
}