package io.medatarun.platform.db

import io.medatarun.platform.kernel.ServiceContributionPoint

/**
 * Declares the schema evolution owned by one extension.
 *
 * The runner creates one [DbMigrationContext] per plugin and lets the plugin apply whatever versions
 * are still missing from the current database state.
 */
interface DbMigration: ServiceContributionPoint {

    /**
     * Name of the shard of data that this plugin handles. This must be stable
     * key. It is used in the database changelog to identify the set of data
     * managed by this migration and keep track of version numbers
     */
    val pluginId: String

    /**
     * Indicates the current version number we should upgrade to
     */
    fun latestVersion(): Int

    /**
     * Called when the plugin had never been installed before, or said otherwise
     * when the [pluginId] is not yet tracked in the migration changelog.
     *
     * After [install] data is marked to be in the [latestVersion] in changelog.
     */
    fun install(ctx: DbMigrationContext)

    /**
     * Called each time a version needs to be upgraded.
     *
     * If [latestVersion] is 5, for example, and in the migration changelog we
     * see 2 as the latest installed version for this plugin, then the migration
     * manager will call [applyVersion] with version 3 (latest+1), 4, 5 successively.
     *
     * The [version] number is the version to upgrade to. Note that you will
     * never see "1" because it would mean it is a fresh installation, then the
     * [install] method would have been called instead. In that case, the data
     * would directly migrate to the latest version once for all.
     */
    fun applyVersion(version: Int, ctx: DbMigrationContext)

    /**
     * This is called each time the migration process runs, after we are sure
     * that this module is already at [latestVersion].
     *
     * Typical usage is to clean up data, remove keys, rebuild indexes, to some checkup, etc.
     * Any maintenance operations that need to be done at startup.
     *
     * Note that any exceptions that may occur here will prevent the platform
     * from starting.
     */
    fun applyAlwaysAfterMigrations(ctx: DbMigrationContext) {
        // default empty implementation
    }
}

