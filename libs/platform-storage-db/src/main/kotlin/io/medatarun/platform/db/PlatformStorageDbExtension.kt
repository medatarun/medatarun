package io.medatarun.platform.db

import io.medatarun.platform.db.adapters.DbConnectionFactoryImpl
import io.medatarun.platform.db.adapters.DbTransactionManagerImpl
import io.medatarun.platform.db.adapters.ExtensionRegistryDbProvider
import io.medatarun.platform.db.internal.DbMigrationCheckerImpl
import io.medatarun.platform.db.internal.DbMigrationRunner
import io.medatarun.platform.kernel.*

class PlatformStorageDbExtension : MedatarunExtension {
    override val id: ExtensionId = "platform-storage-db"

    override fun initServices(ctx: MedatarunServiceCtx) {
        val extensionRegistry = ctx.getService(ExtensionRegistry::class)
        val dbProvider = ExtensionRegistryDbProvider(extensionRegistry)
        val txManager = DbTransactionManagerImpl(dbProvider)
        val connectionFactory = DbConnectionFactoryImpl(dbProvider, txManager)
        ctx.register(DbTransactionManager::class, txManager)
        ctx.register(DbConnectionFactory::class, connectionFactory)
        ctx.register(DbMigrationRunner::class, DbMigrationRunner(extensionRegistry, connectionFactory, txManager))
        ctx.register(DbMigrationChecker::class, DbMigrationCheckerImpl(connectionFactory))
    }

    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint("$id.db-provider", DbProvider::class)
        ctx.registerContributionPoint("$id.db-migration", DbMigration::class)
        ctx.register(PlatformStartedListener::class, object : PlatformStartedListener {
            override fun onPlatformStarted(ctx: PlatformStartedCtx) {
                val runner = ctx.services.getService(DbMigrationRunner::class)
                runner.runAll()
            }
        })
    }
}
