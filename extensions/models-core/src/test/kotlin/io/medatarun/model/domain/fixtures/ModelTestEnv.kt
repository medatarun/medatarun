package io.medatarun.model.domain.fixtures

import com.google.common.jimfs.Jimfs
import io.medatarun.actions.ActionsExtension
import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.actions.ports.needs.*
import io.medatarun.model.ModelExtension
import io.medatarun.model.ModelExtensionConfigProd
import io.medatarun.model.actions.ModelAction
import io.medatarun.model.actions.ModelActionProvider
import io.medatarun.model.domain.*
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.infra.db.ModelStorageDb
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.model.security.ModelSecurityPermissionsProvider
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
import io.medatarun.tags.core.adapters.security.TagSecurityPermissionsProvider
import io.medatarun.tags.core.domain.*
import io.medatarun.type.commons.id.Id
import io.medatarun.types.TypeSystemExtension
import kotlin.reflect.full.findAnnotation
import kotlin.test.assertEquals

class ModelTestEnv(otherExtesions: List<MedatarunExtension> = emptyList()) {


    private val appActorResolver = AppActorResolverTest()

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

    private val storageDb = ModelStorageDb(dbConnectionFactory, ModelExtensionConfigProd().modelClock)
    private val actionPlatform get() = platform.services.getService<ActionPlatform>()

    val principal
        get() = appActorResolver.testPrincipal

    fun dispatch(action: ModelAction): Any? {
        val request = ActionRequest(
            ModelActionProvider.ACTION_GROUP_KEY,
            action::class.findAnnotation<ActionDoc>()!!.key,
            ActionPayload.AsRaw(action)
        )
        return actionPlatform.invoker.handleInvocation(request, appActorResolver.testActionRequestContext)
    }

    fun dispatchTag(action: TagAction): Any? {
        val request = ActionRequest(
            TagActionProvider.ACTION_GROUP_KEY,
            action::class.findAnnotation<ActionDoc>()!!.key,
            ActionPayload.AsRaw(action)
        )
        return actionPlatform.invoker.handleInvocation(request, appActorResolver.testActionRequestContext)

    }

    // -------------------------------------------------------------------------
    //  Action methods
    // -------------------------------------------------------------------------

    // Model_Create
    fun modelCreate(
        key: ModelKey,
        name: TextSingleLine = TextSingleLine(key.value),
        description: TextMarkdown? = null,
        version: ModelVersion? = ModelVersion("1.0.0")
    ): Any? {
        return dispatch(
            ModelAction.Model_Create(
                key = key,
                name = name,
                description = description,
                version = version
            )
        )
    }

    // Type_Create
    fun typeCreate(
        modelRef: ModelRef,
        typeKey: TypeKey,
        name: TextSingleLine? = null,
        description: TextMarkdown? = null
    ): Any? {
        return dispatch(
            ModelAction.Type_Create(
                modelRef = modelRef,
                typeKey = typeKey,
                name = name,
                description = description
            )
        )
    }

    // Entity_Create2
    fun entityCreate(
        modelRef: ModelRef,
        entityKey: EntityKey,
        name: TextSingleLine? = null,
        description: TextMarkdown? = null,
        documentationHome: String? = null
    ): Any? {
        return dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef,
                entityKey = entityKey,
                name = name,
                description = description,
                documentationHome = documentationHome
            )
        )
    }

    // EntityAttribute_Create
    fun entityAttributeCreate(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attributeKey: AttributeKey,
        type: TypeRef,
        name: TextSingleLine? = null,
        optional: Boolean = false,
        description: TextMarkdown? = null
    ): Any? {
        return dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                name = name,
                attributeKey = attributeKey,
                type = type,
                optional = optional,
                description = description
            )
        )
    }

    // Relationship_Create
    fun relationshipCreate(
        modelRef: ModelRef,
        relationshipKey: RelationshipKey,
        roleAKey: RelationshipRoleKey,
        roleAEntityRef: EntityRef,
        roleBKey: RelationshipRoleKey,
        roleBEntityRef: EntityRef,
        name: TextSingleLine? = null,
        description: TextMarkdown? = null,
        roleAName: TextSingleLine? = null,
        roleACardinality: RelationshipCardinality = RelationshipCardinality.One,
        roleBName: TextSingleLine? = null,
        roleBCardinality: RelationshipCardinality = RelationshipCardinality.Many
    ): Any? {
        return dispatch(
            ModelAction.Relationship_Create(
                modelRef = modelRef,
                relationshipKey = relationshipKey,
                name = name,
                description = description,
                roleAKey = roleAKey,
                roleAEntityRef = roleAEntityRef,
                roleAName = roleAName,
                roleACardinality = roleACardinality,
                roleBKey = roleBKey,
                roleBEntityRef = roleBEntityRef,
                roleBName = roleBName,
                roleBCardinality = roleBCardinality
            )
        )
    }

    // RelationshipRole_Create
    fun relationshipRoleCreate(
        modelRef: ModelRef,
        relationshipRef: RelationshipRef,
        roleKey: RelationshipRoleKey,
        roleEntityRef: EntityRef,
        roleName: TextSingleLine? = null,
        roleCardinality: RelationshipCardinality = RelationshipCardinality.One
    ): Any? {
        return dispatch(
            ModelAction.RelationshipRole_Create(
                modelRef = modelRef,
                relationshipRef = relationshipRef,
                roleKey = roleKey,
                roleEntityRef = roleEntityRef,
                roleName = roleName,
                roleCardinality = roleCardinality
            )
        )
    }

    // RelationshipAttribute_Create
    fun relationshipAttributeCreate(
        modelRef: ModelRef,
        relationshipRef: RelationshipRef,
        attributeKey: AttributeKey,
        type: TypeRef = typeRefKey("String"),
        name: TextSingleLine? = null,
        optional: Boolean = false,
        description: TextMarkdown? = null
    ): Any? {
        return dispatch(
            ModelAction.RelationshipAttribute_Create(
                modelRef = modelRef,
                relationshipRef = relationshipRef,
                name = name,
                attributeKey = attributeKey,
                type = type,
                optional = optional,
                description = description
            )
        )
    }

    // BusinessKey_Create
    fun businessKeyCreate(
        modelRef: ModelRef,
        key: BusinessKeyKey,
        entityRef: EntityRef,
        participants: List<EntityAttributeRef>,
        name: TextSingleLine? = null,
        description: TextMarkdown? = null
    ): Any? {
        return dispatch(
            ModelAction.BusinessKey_Create(
                modelRef = modelRef,
                name = name,
                key = key,
                description = description,
                entityRef = entityRef,
                participants = participants
            )
        )
    }


    // -------------------------------------------------------------------------
    //  Other
    // -------------------------------------------------------------------------

    fun loginAsAdmin() {
        appActorResolver.loginAsAdmin()

    }

    /**
     * Runs read assertions twice:
     * once on current projections, then once after rebuilding model projections from events.
     */
    fun replayWithRebuild(block: () -> Unit) {
        block()
        val previousPrincipal = appActorResolver.testPrincipal
        try {
            loginAsAdmin()
            dispatch(ModelAction.MaintenanceRebuildCaches())
        } finally {
            appActorResolver.loginAs(previousPrincipal)

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
        val modelId = queries.findModelAggregate(modelRef).id
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
        assertEquals(
            expectedVersion,
            foundVersion,
            "Model expected version should be $expectedVersion but was $foundVersion"
        )
    }

    /**
     * Returns the latest raw event stored for this model.
     * This bypasses query-layer history filtering logic and reads persisted events directly.
     */
    fun findLastStoredModelChangeEvent(modelRef: ModelRef): ModelChangeEvent {
        val modelId = storageDb.findModel(modelRef).id
        return findLastStoredModelChangeEvent(modelId)
    }

    fun findLastStoredModelChangeEvent(modelId: ModelId): ModelChangeEvent {
        return storageDb.findLastModelChangeEvent(modelId)
    }

    /**
     * Returns all raw events stored for this model.
     * This bypasses query-layer history filtering logic and reads persisted events directly.
     */
    fun findAllModelEvents(modelId: ModelId): List<ModelChangeEvent> {
        return storageDb.findAllModelChangeEvent(modelId)
    }


    companion object {

        private val allPermissions = (TagSecurityPermissionsProvider().getPermissions() + ModelSecurityPermissionsProvider().getPermissions()).map { it.key }.toSet()

        private val testPrincipalUser = object : AppPrincipal {
            override val id: AppActorId = Id.generate(::AppActorId)
            override val issuer: String = ""
            override val subject: String = ""
            override val isAdmin: Boolean = false
            override val fullname: String = "user"
            override val permissions = allPermissions

        }
        private val testPrincipalAdmin = object : AppPrincipal {
            override val id: AppActorId = Id.generate(::AppActorId)
            override val issuer: String = ""
            override val subject: String = ""
            override val isAdmin: Boolean = true
            override val fullname: String = "admin"
            override val permissions = allPermissions
        }
        private class AppActorResolverTest: AppActorResolver {

            var testPrincipal: AppPrincipal = testPrincipalUser
                private set

            override fun resolve(appActorId: AppActorId): AppActor {
                return object : AppActor {
                    override val id: AppActorId
                        get() = testPrincipal.id
                    override val displayName: String
                        get() = testPrincipal.fullname

                }
            }

            fun loginAsAdmin() {
                testPrincipal = testPrincipalAdmin
            }

            fun loginAs(other: AppPrincipal) {
                testPrincipal = other
            }

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
}
