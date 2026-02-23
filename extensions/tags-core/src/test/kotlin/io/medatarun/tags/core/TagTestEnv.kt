package io.medatarun.tags.core

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.adapters.DbConnectionFactoryImpl
import io.medatarun.platform.db.adapters.DbTransactionManagerImpl
import io.medatarun.platform.db.sqlite.DbProviderSqlite
import io.medatarun.platform.kernel.EventObserver
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.internal.EventSystemImpl
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.actions.TagActionProvider
import io.medatarun.tags.core.adapters.TagStorageSQLite
import io.medatarun.tags.core.domain.TagBeforeDeleteEvt
import io.medatarun.tags.core.domain.TagCmd
import io.medatarun.tags.core.domain.TagCmds
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.domain.TagScopeBeforeDeleteEvent
import io.medatarun.tags.core.fixtures.RecipeService
import io.medatarun.tags.core.fixtures.RecipeTagScopeManager
import io.medatarun.tags.core.fixtures.VehicleService
import io.medatarun.tags.core.fixtures.VehicleTagScopeManager
import io.medatarun.tags.core.fixtures.recipeScopeRef
import io.medatarun.tags.core.fixtures.vehicleScopeRef
import io.medatarun.tags.core.internal.*
import io.medatarun.tags.core.ports.needs.TagScopeManager
import io.medatarun.tags.core.ports.needs.TagScopeManagerResolver
import kotlin.reflect.KClass

class TagTestEnv(
    val extraScopeManagers: List<TagScopeManager> = emptyList(),
    val extraListeners: List<EventObserver<TagBeforeDeleteEvt>> = emptyList()
) {
    val provider = TagActionProvider()
    var actionCtx = ActionCtxWithActor()
    val sqliteDbProvider = DbProviderSqlite.randomDb()
    val txManager = DbTransactionManagerImpl(sqliteDbProvider)
    val dbConnectionFactory: DbConnectionFactory = DbConnectionFactoryImpl(sqliteDbProvider, txManager)
    val eventSystem = EventSystemImpl().also { e ->
        extraListeners.forEach { e.registerObserver(TagBeforeDeleteEvt::class, it) }
    }

    // Keeps a connection alive until this class lifecycle ends
    @Suppress("unused")
    val dbConnectionKeeper = dbConnectionFactory.getConnection()

    val tagStorage = TagStorageSQLite(dbConnectionFactory).also { it.initSchema() }

    // Recipe domain
    val recipeEventNotifier = eventSystem.createNotifier(TagScopeBeforeDeleteEvent::class)
    val recipeService = RecipeService(onBeforeDelete = { id ->
        recipeEventNotifier.fire(TagScopeBeforeDeleteEvent(recipeScopeRef(id)))
    })
    val recipeTagScopeManager = RecipeTagScopeManager(recipeService)

    // Vehicle domain
    val vehicleServiceDeletedNotifier = eventSystem.createNotifier(TagScopeBeforeDeleteEvent::class)
    val vehicleService = VehicleService { id ->
        vehicleServiceDeletedNotifier.fire(TagScopeBeforeDeleteEvent(vehicleScopeRef(id)))
    }
    val vehicleTagScopeManager = VehicleTagScopeManager(vehicleService)


    val tagScopeManagers = listOf(
        recipeTagScopeManager,
        vehicleTagScopeManager
    ) + extraScopeManagers

    val tagScopeRegistry = TagScopeRegistryImpl(
        object : TagScopeManagerResolver {
            override fun findScopeManagers(): List<TagScopeManager> {
                return tagScopeManagers
            }
        }
    )

    val tagEvents = TagCmdsEventsHandler(eventSystem)
    val tagScopes = TagScopesImpl(tagScopeRegistry)
    val tagCmds = TagCmdsImpl(tagStorage, tagScopes, tagEvents, txManager)
    val tagQueries = TagQueriesImpl(tagStorage)


    init {
        eventSystem.registerObserver(TagScopeBeforeDeleteEvent::class) { evt ->
            tagCmds.dispatch(TagCmd.TagScopeDelete(evt.tagScopeRef))
        }

        eventSystem.registerObserver(TagBeforeDeleteEvt::class, object : EventObserver<TagBeforeDeleteEvt> {
            override fun onEvent(evt: TagBeforeDeleteEvt) {
                recipeService.removeTagEverywhere(evt.id)
            }
        })

        eventSystem.registerObserver(TagBeforeDeleteEvt::class, object : EventObserver<TagBeforeDeleteEvt> {
            override fun onEvent(evt: TagBeforeDeleteEvt) {
                vehicleService.removeTagEverywhere(evt.id)
            }
        })


    }

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
