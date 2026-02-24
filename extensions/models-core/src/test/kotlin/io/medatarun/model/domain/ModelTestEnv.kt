package io.medatarun.model.domain


import com.google.common.jimfs.Jimfs
import io.medatarun.actions.ActionsExtension
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.model.ModelExtension
import io.medatarun.model.actions.ModelAction
import io.medatarun.model.actions.ModelActionProvider
import io.medatarun.model.infra.ModelRepositoryInMemory
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.platform.db.PlatformStorageDbExtension
import io.medatarun.platform.db.sqlite.DbProviderSqlite
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension.Companion.JDBC_URL_PROPERTY
import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunConfig
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.PlatformBuilder
import io.medatarun.security.SecurityExtension
import io.medatarun.tags.core.TagsCoreExtension
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.actions.TagActionProvider
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.types.TypeSystemExtension
import kotlin.reflect.KClass

class ModelStorageInMemoryExtension(
    val repos: List<ModelRepository>
) : MedatarunExtension {
    override val id: ExtensionId = "model-storage-in-memory"
    override fun init(ctx: MedatarunExtensionCtx) {
        repos.forEach {
            ctx.register(ModelRepository::class, it)
        }

    }
}

class ModelTestEnv(val repositories: List<ModelRepositoryInMemory> = emptyList()) {
    private val extensions = listOf(
        TypeSystemExtension(),
        SecurityExtension(),
        ActionsExtension(),
        PlatformStorageDbExtension(),
        PlatformStorageDbSqliteExtension(),
        TagsCoreExtension(),
        ModelExtension(),
        ModelStorageInMemoryExtension(repositories)
    )
    val platform = PlatformBuilder(
        config = MedatarunConfig.createTempConfig(
            Jimfs.newFileSystem(),
            mapOf(
                JDBC_URL_PROPERTY to DbProviderSqlite.randomDbUrl()
            )
        ),
        extensions = extensions
    ).buildAndStart()

    val queries
        get() = platform.services.getService(ModelQueries::class)
    val tagQueries
        get() = platform.services.getService(TagQueries::class)

    private val modelActionProvider = ModelActionProvider(platform.config.createResourceLocator())
    private val tagActionProvider = TagActionProvider()
    private val actionCtx = object : ActionCtx {
        override val extensionRegistry: ExtensionRegistry = platform.extensions
        override fun dispatchAction(req: ActionRequest): Any =
            throw IllegalStateException("Should not be called in tests")

        override fun <T : Any> getService(type: KClass<T>): T = platform.services.getService(type)
        override val principal: ActionPrincipalCtx
            get() = throw IllegalStateException("Should not be called")
    }

    fun dispatch(action: ModelAction) {
        modelActionProvider.dispatch(action, actionCtx)
    }

    fun dispatchTag(action: TagAction) {
        tagActionProvider.dispatch(action, actionCtx)
    }
}

fun createEnv(
    repositories: List<ModelRepositoryInMemory> = listOf(ModelRepositoryInMemory("repo"))
): ModelTestEnv {
    return ModelTestEnv(repositories)

}
