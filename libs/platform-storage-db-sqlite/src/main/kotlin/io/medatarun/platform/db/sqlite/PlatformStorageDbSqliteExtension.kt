package io.medatarun.platform.db.sqlite

import io.medatarun.platform.db.DbProvider
import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import kotlin.io.path.createDirectories

class PlatformStorageDbSqliteExtension : MedatarunExtension {
    override val id: ExtensionId = "platform-storage-db-sqlite"
    override fun initContributions(ctx: MedatarunExtensionCtx) {
        // WARNING: SQLite doesn't support Java Filesystems, any database created will be created for real
        // on the hard drive.
        val url = configuredDatabase(ctx) ?: defaultDatabase(ctx)
        val dbConnectionFactory = DbProviderSqlite(url)
        ctx.registerContribution(DbProvider::class, dbConnectionFactory)
    }

    private fun configuredDatabase(ctx: MedatarunExtensionCtx): String? {
        return ctx.getConfigProperty(JDBC_URL_PROPERTY)
    }

    override fun initServices(ctx: MedatarunServiceCtx) {
    }

    fun defaultDatabase(ctx: MedatarunExtensionCtx): String {
        val dbPath = ctx.resolveApplicationHomePath("data/database.db").toAbsolutePath()
        if (dbPath.startsWith("/"))
            dbPath.parent.createDirectories()
        return "jdbc:sqlite:file:$dbPath"

    }

    companion object {
        const val JDBC_URL_PROPERTY = "medatarun.storage.datasource.jdbc.url"
    }
}

