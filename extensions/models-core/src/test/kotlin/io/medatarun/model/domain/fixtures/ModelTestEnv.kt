package io.medatarun.model.domain.fixtures

import com.google.common.jimfs.Jimfs
import io.medatarun.actions.ActionsExtension
import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.actions.ports.needs.*
import io.medatarun.model.ModelExtension
import io.medatarun.model.ModelExtensionConfigProd
import io.medatarun.model.actions.ModelAction
import io.medatarun.model.actions.ModelActionProvider
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelRef
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.infra.db.ModelStorageDb
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbMigrationChecker
import io.medatarun.platform.db.PlatformStorageDbExtension
import io.medatarun.platform.db.postgresql.PlatformStorageDbPostgresqlExtension
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.platform.db.testkit.TestDbConfig
import io.medatarun.platform.kernel.MedatarunConfig
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.PlatformBuilder
import io.medatarun.platform.kernel.getService
import io.medatarun.security.*
import io.medatarun.tags.core.TagsCoreExtension
import io.medatarun.tags.core.actions.TagAction
import io.medatarun.tags.core.actions.TagActionProvider
import io.medatarun.tags.core.adapters.security.TagLocalManageRole
import io.medatarun.tags.core.adapters.security.TagGroupManageRole
import io.medatarun.tags.core.adapters.security.TagGlobalManageRole
import io.medatarun.tags.core.domain.*
import io.medatarun.type.commons.id.Id
import io.medatarun.types.TypeSystemExtension
import kotlin.reflect.full.findAnnotation
import kotlin.test.assertEquals

class ModelTestEnv(otherExtesions: List<MedatarunExtension> = emptyList()) {
    private val extensions = listOf(
        TypeSystemExtension(),
        SecurityExtension(SecurityExtensionConfig(appActorResolver)),
        ActionsExtension(),
        PlatformStorageDbExtension(),
        PlatformStorageDbSqliteExtension(),
        PlatformStorageDbPostgresqlExtension(),
        TagsCoreExtension(),
        ModelExtension()
    ).plus(otherExtesions)
    val platform = PlatformBuilder(
        config = MedatarunConfig.createTempConfig(
            Jimfs.newFileSystem(),
            TestDbConfig().testDatabaseProperties()
        ),
        extensions = extensions
    ).buildAndStart()

    val queries
        get() = platform.services.getService(ModelQueries::class)
    val tagQueries
        get() = platform.services.getService(TagQueries::class)
    val dbMigrationChecker
        get() = platform.services.getService(DbMigrationChecker::class)
    val dbConnectionFactory
        get() = platform.services.getService(DbConnectionFactory::class)
    val storageDb
        get() = ModelStorageDb(dbConnectionFactory, ModelExtensionConfigProd().modelClock)
    private val actionPlatform get() = platform.services.getService<ActionPlatform>()

    fun dispatch(action: ModelAction): Any? {
        val request = ActionRequest(
            ModelActionProvider.ACTION_GROUP_KEY,
            action::class.findAnnotation<ActionDoc>()!!.key,
            ActionPayload.AsRaw(action)
        )
        return actionPlatform.invoker.handleInvocation(request, testActionRequestContext)
    }

    fun dispatchTag(action: TagAction): Any? {
        val request = ActionRequest(
            TagActionProvider.ACTION_GROUP_KEY,
            action::class.findAnnotation<ActionDoc>()!!.key,
            ActionPayload.AsRaw(action)
        )
        return actionPlatform.invoker.handleInvocation(request, testActionRequestContext)

    }

    fun loginAsAdmin() {
        testPrincipal = testPrincipalAdmin
    }

    /**
     * Runs read assertions twice:
     * once on current projections, then once after rebuilding model projections from events.
     */
    fun replayWithRebuild(block: () -> Unit) {
        block()
        val previousPrincipal = testPrincipal
        try {
            loginAsAdmin()
            dispatch(ModelAction.MaintenanceRebuildCaches())
        } finally {
            testPrincipal = previousPrincipal
        }
        block()
    }

    /**
     * Creates a global tag in global scope and returns the created tag from queries.
     * Tests use this helper to attach global tags to model artifacts.
     */
    fun createGlobalTag(groupKeyValue: String, tagKeyValue: String): Tag {
        val groupKey = TagGroupKey(groupKeyValue)
        val tagKey = TagKey(tagKeyValue)
        val tagRef = TagRef.ByKey(
            scopeRef = TagScopeRef.Global,
            groupKey = groupKey,
            key = tagKey
        )

        dispatchTag(TagAction.TagGroupCreate(groupKey, null, null))
        dispatchTag(TagAction.TagGlobalCreate(TagGroupRef.ByKey(groupKey), tagKey, null, null))

        return tagQueries.findTagByRef(tagRef)
    }

    /**
     * Creates a local tag inside the provided model scope and returns the created tag.
     * This keeps scope checks explicit in tests that validate tag attachment rules.
     */
    fun createLocalTagInModelScope(modelRef: ModelRef, tagKeyValue: String): Tag {
        val modelId = queries.findModel(modelRef).id
        val scopeRef = ModelTagResolver.modelTagScopeRef(modelId)
        val tagKey = TagKey(tagKeyValue)
        val tagRef = TagRef.ByKey(
            scopeRef = scopeRef,
            groupKey = null,
            key = tagKey
        )
        dispatchTag(TagAction.TagLocalCreate(scopeRef, tagKey, null, null))
        return tagQueries.findTagByRef(tagRef)
    }

    /**
     * Asserts that the specified model has this version
     */
    fun assertUniqueVersion(expectedVersion: ModelVersion, modelId: ModelId) {
        val versions = queries.findModelVersions(ModelRef.modelRefId(modelId))
        assertEquals(1, versions.size, "Model should have exactly one version released")
        val foundVersion = versions.first().modelVersion
        assertEquals(expectedVersion, foundVersion, "Model expected version should be $expectedVersion but was $foundVersion")
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
        var testPrincipal: AppPrincipal = testPrincipalUser
            private set
        val testPrincipalCtx = object : ActionPrincipalCtx {
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
            override val source: String
                get() = "test"
        }
    }
}
