package io.medatarun.tags.core

import io.medatarun.actions.ActionsExtension
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.platform.db.PlatformStorageDbExtension
import io.medatarun.platform.db.sqlite.DbProviderSqlite
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension.Companion.JDBC_URL_PROPERTY
import io.medatarun.platform.kernel.*
import io.medatarun.security.SecurityExtension
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.actions.TagActionProvider
import io.medatarun.tags.core.domain.TagBeforeDeleteEvt
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.domain.TagScopeBeforeDeleteEvent
import io.medatarun.tags.core.fixtures.*
import io.medatarun.tags.core.ports.needs.TagScopeManager
import io.medatarun.types.TypeSystemExtension
import java.nio.file.Path
import kotlin.reflect.KClass

class VehicleExtension : MedatarunExtension {
    override val id: String = "vehicle"
    override fun init(ctx: MedatarunExtensionCtx) {
        val vehicleService = ctx.getService(VehicleService::class)
        val vehicleTagScopeManager = VehicleTagScopeManager(vehicleService)
        ctx.register(TagScopeManager::class, vehicleTagScopeManager)
    }

    override fun initServices(ctx: MedatarunServiceCtx) {
        val eventSystem = ctx.getService(EventSystem::class)
        val vehicleServiceDeletedNotifier = eventSystem.createNotifier(TagScopeBeforeDeleteEvent::class)
        val vehicleService = VehicleService { id ->
            vehicleServiceDeletedNotifier.fire(TagScopeBeforeDeleteEvent(vehicleScopeRef(id)))
        }

        eventSystem.registerObserver(TagBeforeDeleteEvt::class, object : EventObserver<TagBeforeDeleteEvt> {
            override fun onEvent(evt: TagBeforeDeleteEvt) {
                vehicleService.removeTagEverywhere(evt.id)
            }
        })

        ctx.register(VehicleService::class, vehicleService)
    }
}

class RecipeExtension : MedatarunExtension {
    override val id: String = "recipe"
    override fun init(ctx: MedatarunExtensionCtx) {
        val recipeService = ctx.getService(RecipeService::class)
        val recipeTagScopeManager = RecipeTagScopeManager(recipeService)
        ctx.register(TagScopeManager::class, recipeTagScopeManager)
    }

    override fun initServices(ctx: MedatarunServiceCtx) {
        val eventSystem = ctx.getService(EventSystem::class)
        val recipeEventNotifier = eventSystem.createNotifier(TagScopeBeforeDeleteEvent::class)
        val recipeService = RecipeService(onBeforeDelete = { id ->
            recipeEventNotifier.fire(TagScopeBeforeDeleteEvent(recipeScopeRef(id)))
        })

        eventSystem.registerObserver(TagBeforeDeleteEvt::class, object : EventObserver<TagBeforeDeleteEvt> {
            override fun onEvent(evt: TagBeforeDeleteEvt) {
                recipeService.removeTagEverywhere(evt.id)
            }
        })

        ctx.register(RecipeService::class, recipeService)
    }
}

class ExtraExtension(
    val extraScopeManagers: List<TagScopeManager> = emptyList(),
    val extraListeners: List<EventObserver<TagBeforeDeleteEvt>> = emptyList()
) : MedatarunExtension {
    override val id: String = "extra"

    override fun initServices(ctx: MedatarunServiceCtx) {
        val eventSystem = ctx.getService(EventSystem::class)
        for (observer in extraListeners) {
            eventSystem.registerObserver(TagBeforeDeleteEvt::class, observer)
        }
    }

    override fun init(ctx: MedatarunExtensionCtx) {
        extraScopeManagers.forEach { ctx.register(TagScopeManager::class, it) }
    }
}


class TagTestEnv(
    val extraScopeManagers: List<TagScopeManager> = emptyList(),
    val extraListeners: List<EventObserver<TagBeforeDeleteEvt>> = emptyList()
) {

    val config = object : MedatarunConfig {
        override val applicationHomeDir: Path = Path.of("/tmp/medatarun")
        override val projectDir: Path = Path.of("/tmp/medatarun/project")
        override fun getProperty(key: String): String? {
            if (key == JDBC_URL_PROPERTY) return DbProviderSqlite.randomDbUrl()
            return null
        }

        override fun getProperty(key: String, defaultValue: String): String {
            return getProperty(key) ?: defaultValue
        }

        override fun createResourceLocator(): ResourceLocator = throw IllegalStateException("Test should not use this")
    }
    val extensions = listOf(
        TypeSystemExtension(),
        ActionsExtension(),
        SecurityExtension(),
        PlatformStorageDbExtension(),
        PlatformStorageDbSqliteExtension(),
        TagsCoreExtension(),
        VehicleExtension(),
        RecipeExtension(),
        ExtraExtension(extraScopeManagers, extraListeners)
    )
    val platform = PlatformBuilder(config, extensions).buildAndStart()

    val tagQueries get() = platform.services.getService<TagQueries>()
    val vehicleService get() = platform.services.getService<VehicleService>()
    val recipeService get() = platform.services.getService<RecipeService>()

    private val provider = TagActionProvider()

    fun dispatch(cmd: TagAction) = provider.dispatch(cmd, object : ActionCtx {
        override val extensionRegistry: ExtensionRegistry = platform.extensions
        override fun dispatchAction(req: ActionRequest): Any =
            throw IllegalStateException("Should not be called in tests")

        override fun <T : Any> getService(type: KClass<T>): T = platform.services.getService(type)
        override val principal: ActionPrincipalCtx
            get() = throw TagTestIllegalStateException("Should not be called")

    })

}
