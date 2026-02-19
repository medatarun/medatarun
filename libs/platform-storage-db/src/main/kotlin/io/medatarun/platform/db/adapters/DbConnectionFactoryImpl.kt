package io.medatarun.platform.db.adapters

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbProvider
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.internal.ExtensionRegistryImpl
import java.sql.Connection

class DbConnectionFactoryImpl(val extensionRegistry: ExtensionRegistry): DbConnectionFactory {

    private val dbProvider = lazy {
        extensionRegistry.findContributionsFlat(DbProvider::class).firstOrNull() ?: throw DbConnectionFactoryNoDbProviderFoundException()
    }

    override fun getConnection(): Connection {
        return dbProvider.value.getConnection()
    }

    class DbConnectionFactoryNoDbProviderFoundException : MedatarunException("No database provider found. Please provide a plugin with a DbProvider contribution like 'platform-storage-db-sqlite'")
}