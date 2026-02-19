package io.medatarun.platform.db

import io.medatarun.platform.db.adapters.DbConnectionFactoryImpl
import io.medatarun.platform.kernel.*

class PlatformStorageDbExtension: MedatarunExtension {
    override val id: ExtensionId = "platform-storage-db"

    override fun initServices(ctx: MedatarunServiceCtx) {
        val extensionRegistry = ctx.getService(ExtensionRegistry::class)
        ctx.register(DbConnectionFactory::class, DbConnectionFactoryImpl(extensionRegistry))
    }

    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint("$id.db-provider",DbProvider::class)
    }
}