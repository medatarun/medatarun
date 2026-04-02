package io.medatarun.platform.db.adapters

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.DbDialect
import io.medatarun.platform.db.DbProvider
import io.medatarun.platform.kernel.ExtensionRegistry
import java.sql.Connection

/**
 * Adapter used at composition time so lower-level DB services can depend only on [DbProvider].
 */
class ExtensionRegistryDbProvider(
    private val extensionRegistry: ExtensionRegistry
) : DbProvider {
    private val delegate = lazy {
        extensionRegistry.findContributionsFlat(DbProvider::class).firstOrNull()
            ?: throw ExtensionRegistryDbProviderNoDbProviderFoundException()
    }

    override val dialect: DbDialect
        get() = delegate.value.dialect

    override fun getConnection(): Connection {
        return delegate.value.getConnection()
    }

    class ExtensionRegistryDbProviderNoDbProviderFoundException :
        MedatarunException("No database provider found. Please provide a plugin with a DbProvider contribution like 'platform-storage-db-sqlite'")
}
