package io.medatarun.tags.core

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.platform.db.DbConnectionFactory
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
import io.medatarun.tags.core.internal.TagCmdsImpl
import io.medatarun.tags.core.internal.TagEventListenerMediator
import io.medatarun.tags.core.internal.TagQueriesImpl
import io.medatarun.tags.core.internal.TagScopeRegistryImpl
import io.medatarun.tags.core.ports.needs.TagScopeManager
import io.medatarun.tags.core.ports.needs.TagScopeManagerResolver
import java.sql.Connection
import kotlin.reflect.KClass

class TagTestEnv {
    val provider = TagActionProvider()
    var actionCtx = ActionCtxWithActor()
    val dbConnectionFactory = object : DbConnectionFactory {
        val sqlite = DbProviderSqlite.randomDb()
        override fun getConnection(): Connection {
            return sqlite.getConnection()
        }
    }

    // Keeps a connection alive until this class lifecycle ends
    @Suppress("unused")
    val dbConnectionKeeper = dbConnectionFactory.getConnection()

    val tagStorage = TagStorageSQLite(dbConnectionFactory).also { it.initSchema() }
    val recipeService = RecipeService()
    val vehicleService = VehicleService()
    val tagScopeManagers = listOf<TagScopeManager>(
        RecipeTagScopeManager(recipeService),
        VehicleTagScopeManager(vehicleService)
    )
    val tagScopeRegistry = TagScopeRegistryImpl(
        object : TagScopeManagerResolver {
            override fun findScopeManagers(): List<TagScopeManager> {
                return tagScopeManagers
            }
        }
    )
    val tagEvents = TagEventListenerMediator(tagScopeRegistry)
    val tagCmds = TagCmdsImpl(tagStorage, tagEvents)
    val tagQueries = TagQueriesImpl(tagStorage)

    fun dispatch(cmd: TagAction) = provider.dispatch(cmd, actionCtx)

    inner class ActionCtxWithActor : ActionCtx {
        override val extensionRegistry: ExtensionRegistry
            get() = throw IllegalStateException("Should not be called")

        override fun dispatchAction(req: ActionRequest): Any? {
            throw IllegalStateException("Should not be called")
        }

        override fun <T : Any> getService(type: KClass<T>): T {
            if (type == TagCmds::class) return tagCmds as T
            if (type == TagQueries::class) return tagQueries as T
            throw IllegalStateException("Unknown service " + type)
        }

        override val principal: ActionPrincipalCtx
            get() = throw IllegalStateException("Should not be called")
    }
}
