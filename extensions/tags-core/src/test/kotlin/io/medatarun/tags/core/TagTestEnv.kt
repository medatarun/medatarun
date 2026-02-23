package io.medatarun.tags.core

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.adapters.DbConnectionFactoryImpl
import io.medatarun.platform.db.adapters.DbTransactionManagerImpl
import io.medatarun.platform.db.sqlite.DbProviderSqlite
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.actions.TagActionProvider
import io.medatarun.tags.core.adapters.TagStorageSQLite
import io.medatarun.tags.core.domain.TagCmds
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.fixtures.RecipeService
import io.medatarun.tags.core.fixtures.RecipeTagScopeManager
import io.medatarun.tags.core.fixtures.VehicleService
import io.medatarun.tags.core.fixtures.VehicleTagScopeManager
import io.medatarun.tags.core.internal.*
import io.medatarun.tags.core.ports.needs.TagScopeManager
import io.medatarun.tags.core.ports.needs.TagScopeManagerResolver
import java.sql.Connection
import kotlin.reflect.KClass

class TagTestEnv(extraScopeManagers: List<TagScopeManager> = emptyList()) {
    val provider = TagActionProvider()
    var actionCtx = ActionCtxWithActor()
    val sqliteDbProvider = DbProviderSqlite.randomDb()
    val txManager = DbTransactionManagerImpl(sqliteDbProvider)
    val dbConnectionFactory: DbConnectionFactory = DbConnectionFactoryImpl(sqliteDbProvider, txManager)

    // Keeps a connection alive until this class lifecycle ends
    @Suppress("unused")
    val dbConnectionKeeper = dbConnectionFactory.getConnection()

    val tagStorage = TagStorageSQLite(dbConnectionFactory).also { it.initSchema() }
    val recipeService = RecipeService()
    val vehicleService = VehicleService()
    val tagScopeManagers = listOf<TagScopeManager>(
        RecipeTagScopeManager(recipeService),
        VehicleTagScopeManager(vehicleService)
    ) + extraScopeManagers
    val tagScopeRegistry = TagScopeRegistryImpl(
        object : TagScopeManagerResolver {
            override fun findScopeManagers(): List<TagScopeManager> {
                return tagScopeManagers
            }
        }
    )
    val tagEvents = TagCmdsEventsHandler(tagScopeRegistry)
    val tagScopes = TagScopesImpl(tagScopeRegistry)
    val tagCmds = TagCmdsImpl(tagStorage, tagScopes, tagEvents, txManager)
    val tagQueries = TagQueriesImpl(tagStorage)

    fun dispatch(cmd: TagAction) = provider.dispatch(cmd, actionCtx)

    inner class ActionCtxWithActor : ActionCtx {
        override val extensionRegistry: ExtensionRegistry
            get() = throw TagTestIllegalStateException("Should not be called")

        override fun dispatchAction(req: ActionRequest): Any? {
            throw TagTestIllegalStateException("Should not be called")
        }

        override fun <T : Any> getService(type: KClass<T>): T {
            if (type == TagCmds::class) return tagCmds as T
            if (type == TagQueries::class) return tagQueries as T
            throw TagTestIllegalStateException("Unknown service $type")
        }

        override val principal: ActionPrincipalCtx
            get() = throw TagTestIllegalStateException("Should not be called")
    }
}
