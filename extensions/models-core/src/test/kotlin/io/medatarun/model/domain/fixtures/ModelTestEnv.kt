package io.medatarun.model.domain.fixtures

import com.google.common.jimfs.Jimfs
import io.medatarun.actions.ActionsExtension
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.model.ModelExtension
import io.medatarun.model.actions.ModelAction
import io.medatarun.model.actions.ModelActionProvider
import io.medatarun.model.domain.ModelRef
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.platform.db.DbMigrationChecker
import io.medatarun.platform.db.PlatformStorageDbExtension
import io.medatarun.platform.db.sqlite.DbProviderSqlite
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunConfig
import io.medatarun.platform.kernel.PlatformBuilder
import io.medatarun.security.SecurityExtension
import io.medatarun.tags.core.TagsCoreExtension
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.actions.TagActionProvider
import io.medatarun.tags.core.domain.Tag
import io.medatarun.tags.core.domain.TagCmds
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagGroupRef
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.types.TypeSystemExtension
import kotlin.reflect.KClass

class ModelTestEnv {
    private val extensions = listOf(
        TypeSystemExtension(),
        SecurityExtension(),
        ActionsExtension(),
        PlatformStorageDbExtension(),
        PlatformStorageDbSqliteExtension(),
        TagsCoreExtension(),
        ModelExtension()
    )
    val platform = PlatformBuilder(
        config = MedatarunConfig.Companion.createTempConfig(
            Jimfs.newFileSystem(),
            mapOf(
                PlatformStorageDbSqliteExtension.Companion.JDBC_URL_PROPERTY to DbProviderSqlite.Companion.randomDbUrl()
            )
        ),
        extensions = extensions
    ).buildAndStart()

    val queries
        get() = platform.services.getService(ModelQueries::class)
    private val cmds
        get() = platform.services.getService(ModelCmds::class)
    val tagQueries
        get() = platform.services.getService(TagQueries::class)
    private val tagCmds
        get() = platform.services.getService(TagCmds::class)
    val dbMigrationChecker
        get() = platform.services.getService(DbMigrationChecker::class)

    private val modelActionProvider = ModelActionProvider(platform.config.createResourceLocator(), platform.extensions, cmds , queries)
    private val tagActionProvider = TagActionProvider(tagCmds, tagQueries)
    private val actionCtx = object : ActionCtx {

        override fun dispatchAction(req: ActionRequest): Any =
            throw IllegalStateException("Should not be called in tests")

        override val principal: ActionPrincipalCtx
            get() = throw IllegalStateException("Should not be called")
    }

    fun dispatch(action: ModelAction) {
        modelActionProvider.dispatch(action, actionCtx)
    }

    fun dispatchResult(action: ModelAction): Any {
        return modelActionProvider.dispatch(action, actionCtx)
    }

    fun dispatchTag(action: TagAction) {
        tagActionProvider.dispatch(action, actionCtx)
    }

    /**
     * Creates a managed tag in global scope and returns the created tag from queries.
     * Tests use this helper to attach globally managed tags to model artifacts.
     */
    fun createManagedTag(groupKeyValue: String, tagKeyValue: String): Tag {
        val groupKey = TagGroupKey(groupKeyValue)
        val tagKey = TagKey(tagKeyValue)
        val tagRef = TagRef.ByKey(
            scopeRef = TagScopeRef.Global,
            groupKey = groupKey,
            key = tagKey
        )

        dispatchTag(TagAction.TagGroupCreate(groupKey, null, null))
        dispatchTag(TagAction.TagManagedCreate(TagGroupRef.ByKey(groupKey), tagKey, null, null))

        return tagQueries.findTagByRef(tagRef)
    }

    /**
     * Creates a free tag inside the provided model scope and returns the created tag.
     * This keeps scope checks explicit in tests that validate tag attachment rules.
     */
    fun createFreeTagInModelScope(modelRef: ModelRef, tagKeyValue: String): Tag {
        val modelId = queries.findModel(modelRef).id
        val scopeRef = ModelTagResolver.Companion.modelTagScopeRef(modelId)
        val tagKey = TagKey(tagKeyValue)
        val tagRef = TagRef.ByKey(
            scopeRef = scopeRef,
            groupKey = null,
            key = tagKey
        )
        dispatchTag(TagAction.TagFreeCreate(scopeRef, tagKey, null, null))
        return tagQueries.findTagByRef(tagRef)
    }
}