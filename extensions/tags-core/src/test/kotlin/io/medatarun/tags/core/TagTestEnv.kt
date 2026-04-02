package io.medatarun.tags.core

import com.google.common.jimfs.Jimfs.newFileSystem
import io.medatarun.actions.ActionsExtension
import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.actions.ports.needs.*
import io.medatarun.platform.db.DbMigrationChecker
import io.medatarun.platform.db.PlatformStorageDbExtension
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.platform.db.testkit.TestDbConfig
import io.medatarun.platform.kernel.*
import io.medatarun.platform.kernel.MedatarunConfig.Companion.createTempConfig
import io.medatarun.security.*
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.actions.TagActionProvider
import io.medatarun.tags.core.adapters.security.TagGlobalManageRole
import io.medatarun.tags.core.adapters.security.TagGroupManageRole
import io.medatarun.tags.core.adapters.security.TagLocalManageRole
import io.medatarun.tags.core.domain.TagBeforeDeleteEvt
import io.medatarun.tags.core.domain.TagLocalScopeBeforeDeleteEvent
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.fixtures.*
import io.medatarun.tags.core.ports.needs.TagScopeManager
import io.medatarun.type.commons.id.Id
import io.medatarun.types.TypeSystemExtension
import kotlin.reflect.full.findAnnotation

class VehicleExtension : MedatarunExtension {
    override val id: String = "vehicle"
    override fun initContributions(ctx: MedatarunExtensionCtx) {
        val vehicleService = ctx.getService(VehicleService::class)
        val vehicleTagScopeManager = VehicleTagScopeManager(vehicleService)
        ctx.registerContribution(TagScopeManager::class, vehicleTagScopeManager)
    }

    override fun initServices(ctx: MedatarunServiceCtx) {
        val eventSystem = ctx.getService(EventSystem::class)
        val vehicleServiceDeletedNotifier = eventSystem.createNotifier(TagLocalScopeBeforeDeleteEvent::class)
        val vehicleService = VehicleService { id ->
            vehicleServiceDeletedNotifier.fire(
                TagLocalScopeBeforeDeleteEvent(
                    vehicleScopeRef(id),
                    TestTraceabilityRecord()
                )
            )
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
    override fun initContributions(ctx: MedatarunExtensionCtx) {
        val recipeService = ctx.getService(RecipeService::class)
        val recipeTagScopeManager = RecipeTagScopeManager(recipeService)
        ctx.registerContribution(TagScopeManager::class, recipeTagScopeManager)
    }

    override fun initServices(ctx: MedatarunServiceCtx) {
        val eventSystem = ctx.getService(EventSystem::class)
        val recipeEventNotifier = eventSystem.createNotifier(TagLocalScopeBeforeDeleteEvent::class)
        val recipeService = RecipeService(onBeforeDelete = { id ->
            recipeEventNotifier.fire(
                TagLocalScopeBeforeDeleteEvent(
                    recipeScopeRef(id),
                    TestTraceabilityRecord()
                )
            )
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

    override fun initContributions(ctx: MedatarunExtensionCtx) {
        extraScopeManagers.forEach { ctx.registerContribution(TagScopeManager::class, it) }
    }
}


class TagTestEnv(
    extraScopeManagers: List<TagScopeManager> = emptyList(),
    extraListeners: List<EventObserver<TagBeforeDeleteEvt>> = emptyList()
) {

    val extensions = listOf(
        TypeSystemExtension(),
        ActionsExtension(),
        SecurityExtension(SecurityExtensionConfig(appActorResolver)),
        PlatformStorageDbExtension(),
        PlatformStorageDbSqliteExtension(),
        TagsCoreExtension(),
        VehicleExtension(),
        RecipeExtension(),
        ExtraExtension(extraScopeManagers, extraListeners)
    )
    val platform = PlatformBuilder(
        createTempConfig(
            newFileSystem(),
            TestDbConfig().testDatabaseProperties()
        ), extensions).buildAndStart()

    val tagQueries get() = platform.services.getService<TagQueries>()
    val vehicleService get() = platform.services.getService<VehicleService>()
    val recipeService get() = platform.services.getService<RecipeService>()
    val dbMigrationChecker get() = platform.services.getService<DbMigrationChecker>()
    private val actionPlatform get() = platform.services.getService<ActionPlatform>()

    fun dispatch(cmd: TagAction): Any? {
        val request = ActionRequest(
            TagActionProvider.ACTION_GROUP_KEY,
            cmd::class.findAnnotation<ActionDoc>()!!.key,
            ActionPayload.AsRaw(cmd)
        )
        return actionPlatform.invoker.handleInvocation(request, testActionRequestContext)
    }

    fun loginAsAdmin() {
        testPrincipal = testPrincipalAdmin
    }

    /**
     * Launches a block of queries and assertions twice
     *
     * One time with the current state of the database.
     * Then we ask to rebuild the projection tables using a maintenance comand.
     * Next we replay the bloc, we should have the same assertions.
     *
     * Using this on key tests allows us to be sure that the projection tables
     * can be rebuilt and have the same results (since the business cases tested
     * are the same).
     *
     */
    fun replayWithRebuild(block: () -> Unit) {
        block()
        val previousPrincipal = testPrincipal
        loginAsAdmin()
        dispatch(TagAction.MaintenanceRebuildCaches())
        testPrincipal = previousPrincipal
        block()
    }

    companion object {

        val appActorResolver = object : AppActorResolver {
            override fun resolve(appActorId: AppActorId): AppActor {
                return object : AppActor {
                    override val id: AppActorId
                        get() = testPrincipal.id
                    override val displayName: String
                        get() = testPrincipal.fullname

                }
            }

        }
        private val testPrincipalUser = object : AppPrincipal {
            override val id: AppActorId = Id.generate(::AppActorId)
            override val issuer: String = ""
            override val subject: String = ""
            override val isAdmin: Boolean = false
            override val fullname: String = "user"
            override val roles: List<AppPrincipalRole> = listOf(
                TagLocalManageRole,
                TagGroupManageRole,
                TagGlobalManageRole
            )
        }
        private val testPrincipalAdmin = object : AppPrincipal {
            override val id: AppActorId = Id.generate(::AppActorId)
            override val issuer: String = ""
            override val subject: String = ""
            override val isAdmin: Boolean = true
            override val fullname: String = "admin"
            override val roles: List<AppPrincipalRole> = listOf(
                TagLocalManageRole,
                TagGroupManageRole,
                TagGlobalManageRole
            )
        }
        private var testPrincipal: AppPrincipal = testPrincipalUser

        private val testPrincipalCtx = object : ActionPrincipalCtx {
            override fun ensureIsAdmin() {
            }

            override fun ensureSignedIn(): AppPrincipal {
                return testPrincipal
            }

            override val principal: AppPrincipal
                get() = testPrincipal
        }

        val testActionRequestContext = object : ActionRequestCtx {
            override val principalCtx: ActionPrincipalCtx
                get() = testPrincipalCtx
            override val source: String = "test"
        }
    }

}
