package io.medatarun.model.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode
import io.medatarun.lang.strings.trimToNull
import io.medatarun.model.actions.history.ModelChangeEventListDto
import io.medatarun.model.actions.history.toModelChangeEventListDto
import io.medatarun.model.actions.tools.AppPrincipalResolver
import io.medatarun.model.domain.*
import io.medatarun.model.domain.diff.*
import io.medatarun.model.domain.search.SearchQuery
import io.medatarun.model.ports.exposed.*
import io.medatarun.model.ports.needs.ModelExporter
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.ResourceLocator
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*

class ModelActionProvider(
    private val resourceLocator: ResourceLocator,
    private val extensionRegistry: ExtensionRegistry,
    private val modelCmds: ModelCmds,
    private val modelQueries: ModelQueries
) : ActionProvider<ModelAction> {

    override val actionGroupKey: String = ACTION_GROUP_KEY


    /**
     * Returns the list of supported commands. Note that we NEVER return the business model's commands
     * but something mode user-facing so that the model can evolve with preserving maximum compatibility
     * with user-facing actions.
     */
    override fun findCommandClass() = ModelAction::class

    override fun dispatch(action: ModelAction, actionCtx: ActionCtx): Any {

        val locale = Locale.getDefault()

        val handler = ModelActionHandler(modelCmds, modelQueries, resourceLocator, locale, actionCtx, extensionRegistry)

        logger.info(action.toString())


        val result = when (action) {

            // ------------------------------------------------------------------------
            // Models
            // ------------------------------------------------------------------------

            is ModelAction.Import -> handler.modelImport(action)
            is ModelAction.Inspect_Json -> handler.modelInspectJson()
            is ModelAction.Compare -> handler.modelCompare(action)
            is ModelAction.Search -> handler.search(action)

            is ModelAction.Model_List -> handler.modelList(action)
            is ModelAction.Model_Export -> handler.modelExport(action)
            is ModelAction.Model_Export_Version -> handler.modelExportVersion(action)

            is ModelAction.Model_Create -> handler.modelCreate(action)
            is ModelAction.Model_Copy -> handler.modelCopy(action)
            is ModelAction.Model_UpdateKey -> handler.modelUpdateKey(action)
            is ModelAction.Model_UpdateName -> handler.modelUpdateName(action)
            is ModelAction.Model_UpdateDescription -> handler.modelUpdateDescription(action)
            is ModelAction.Model_UpdateAuthority -> handler.modelUpdateAuthority(action)
            is ModelAction.Model_UpdateDocumentationHome -> handler.modelUpdateDocumentationHome(action)
            is ModelAction.Model_Release -> handler.modelRelease(action)
            is ModelAction.Model_AddTag -> handler.modelAddTag(action)
            is ModelAction.Model_DeleteTag -> handler.modelDeleteTag(action)
            is ModelAction.Model_Delete -> handler.modelDelete(action)

            // ------------------------------------------------------------------------
            // Types
            // ------------------------------------------------------------------------

            is ModelAction.Type_Create -> handler.typeCreate(action)
            is ModelAction.Type_UpdateName -> handler.typeUpdateName(action)
            is ModelAction.Type_UpdateKey -> handler.typeUpdateKey(action)
            is ModelAction.Type_UpdateDescription -> handler.typeUpdateDescription(action)
            is ModelAction.Type_Delete -> handler.typeDelete(action)

            // ------------------------------------------------------------------------
            // Entities
            // ------------------------------------------------------------------------

            is ModelAction.Entity_Create -> handler.entityCreate(action)
            is ModelAction.Entity_UpdateKey -> handler.entityUpdateKey(action)
            is ModelAction.Entity_UpdateName -> handler.entityUpdateName(action)
            is ModelAction.Entity_UpdateDescription -> handler.entityUpdateDescription(action)
            is ModelAction.Entity_UpdateDocumentationHome -> handler.entityUpdateDocumentationHome(action)
            is ModelAction.Entity_AddTag -> handler.entityAddTag(action)
            is ModelAction.Entity_DeleteTag -> handler.entityDeleteTag(action)
            is ModelAction.Entity_Delete -> handler.entityDelete(action)

            // ------------------------------------------------------------------------
            // Entity attributes
            // ------------------------------------------------------------------------

            is ModelAction.EntityAttribute_Create -> handler.entityAttributeCreate(action)
            is ModelAction.EntityAttribute_UpdateKey -> handler.entityAttributeUpdateKey(action)
            is ModelAction.EntityAttribute_UpdateName -> handler.entityAttributeUpdateName(action)
            is ModelAction.EntityAttribute_UpdateDescription -> handler.entityAttributeUpdateDescription(action)
            is ModelAction.EntityAttribute_UpdateType -> handler.entityAttributeUpdateType(action)
            is ModelAction.EntityAttribute_UpdateOptional -> handler.entityAttributeUpdateOptional(action)
            is ModelAction.EntityAttribute_AddTag -> handler.entityAttributeAddTag(action)
            is ModelAction.EntityAttribute_DeleteTag -> handler.entityAttributeDeleteTag(action)
            is ModelAction.EntityAttribute_Delete -> handler.entityAttributeDelete(action)

            // ------------------------------------------------------------------------
            // Relationships
            // ------------------------------------------------------------------------

            is ModelAction.Relationship_Create -> handler.relationshipCreate(action)
            is ModelAction.Relationship_UpdateKey -> handler.relationshipUpdateKey(action)
            is ModelAction.Relationship_UpdateName -> handler.relationshipUpdateName(action)
            is ModelAction.Relationship_UpdateDescription -> handler.relationshipUpdateDescription(action)
            is ModelAction.Relationship_AddTag -> handler.relationshipAddTag(action)
            is ModelAction.Relationship_DeleteTag -> handler.relationshipDeleteTag(action)
            is ModelAction.Relationship_Delete -> handler.relationshipDelete(action)
            is ModelAction.RelationshipRole_Create -> handler.relationshipRoleCreate(action)
            is ModelAction.RelationshipRole_UpdateKey -> handler.relationshipRoleUpdateKey(action)
            is ModelAction.RelationshipRole_UpdateName -> handler.relationshipRoleUpdateName(action)
            is ModelAction.RelationshipRole_UpdateCardinality -> handler.relationshipRoleUpdateCardinality(action)
            is ModelAction.RelationshipRole_UpdateEntity -> handler.relationshipRoleUpdateEntity(action)
            is ModelAction.RelationshipRole_Delete -> handler.relationshipRoleDelete(action)
            is ModelAction.RelationshipAttribute_Create -> handler.relationshipAttributeCreate(action)
            is ModelAction.RelationshipAttribute_UpdateKey -> handler.relationshipAttributeUpdateKey(action)
            is ModelAction.RelationshipAttribute_UpdateType -> handler.relationshipAttributeUpdateType(action)
            is ModelAction.RelationshipAttribute_UpdateOptional -> handler.relationshipAttributeUpdateOptional(action)
            is ModelAction.RelationshipAttribute_UpdateName -> handler.relationshipAttributeUpdateName(action)
            is ModelAction.RelationshipAttribute_UpdateDescription -> handler.relationshipAttributeUpdateDescription(action)
            is ModelAction.RelationshipAttribute_AddTag -> handler.relationshipAttributeAddTag(action)
            is ModelAction.RelationshipAttribute_DeleteTag -> handler.relationshipAttributeDeleteTag(action)
            is ModelAction.RelationshipAttribute_Delete -> handler.relationshipAttributeDelete(action)

            // History

            is ModelAction.HistoryVersions -> handler.historyVersions(action)
            is ModelAction.HistoryChangesSinceVersion -> handler.historyChangesSinceVersion(action)
        }
        return result
    }


    companion object {
        private val logger = LoggerFactory.getLogger(ModelActionProvider::class.java)

        /**
         * Changing this will break APIs or CLI. It is the name clients are using to reference action group
         */
        const val ACTION_GROUP_KEY = "model"
    }
}


class ModelActionHandler(
    private val modelCmds: ModelCmds,
    private val modelQueries: ModelQueries,
    private val resourceLocator: ResourceLocator,
    private val locale: Locale,
    private val actionCtx: ActionCtx,
    private val extensionRegistry: ExtensionRegistry
) {
    fun dispatch(businessCmd: ModelCmd) {
        val principal = actionCtx.principal.principal ?: throw ModelActionNotAuthorizedException()
        modelCmds.dispatch(ModelCmdEnveloppe(actionCtx.actionInstanceId, principal, businessCmd))
    }

    fun modelImport(action: ModelAction.Import) {
        val contribs = extensionRegistry.findContributionsFlat(ModelImporter::class)
        val resourceLocator = resourceLocator.withPath(action.from)
        val contrib = contribs.firstOrNull { contrib ->
            contrib.accept(action.from, resourceLocator)
        }
        if (contrib == null) {
            throw ModelImportActionNotFoundException(action.from)
        }

        // read model
        val modelData = contrib.toModel(action.from, resourceLocator, action.modelKey, action.modelName)

        // Save imported model
        dispatch(ModelCmd.ImportModel(modelData.model, modelData.tags))
    }


    fun modelInspectJson(): String = ModelInspectJsonAction(modelQueries).process()

    fun modelCompare(action: ModelAction.Compare): ModelCompareDto {
        val diff = modelQueries.diff(
            leftModelRef = action.leftModelRef,
            rightModelRef = action.rightModelRef,
            scope = action.scope
        )
        return toCompareDto(diff)
    }

    fun modelCreate(action: ModelAction.Model_Create) {
        dispatch(
            ModelCmd.CreateModel(
                modelKey = action.modelKey,
                name = action.name,
                description = action.description,
                version = action.version ?: ModelVersion("0.0.1")
            )
        )
    }

    fun modelCopy(action: ModelAction.Model_Copy) {
        dispatch(
            ModelCmd.CopyModel(
                modelRef = action.modelRef,
                modelNewKey = action.modelNewKey
            )
        )
    }

    fun modelUpdateKey(action: ModelAction.Model_UpdateKey) {
        dispatch(
            ModelCmd.UpdateModelKey(
                modelRef = action.modelRef,
                key = action.value
            )
        )
    }

    fun modelUpdateName(action: ModelAction.Model_UpdateName) {
        dispatch(
            ModelCmd.UpdateModelName(
                modelRef = action.modelRef,
                name = action.value,
            )
        )
    }

    fun modelUpdateDescription(action: ModelAction.Model_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateModelDescription(
                modelRef = action.modelRef,
                description = action.value,
            )
        )
    }

    fun modelUpdateAuthority(action: ModelAction.Model_UpdateAuthority) {
        dispatch(
            ModelCmd.UpdateModelAuthority(
                modelRef = action.modelRef,
                authority = action.value
            )
        )
    }

    fun modelUpdateDocumentationHome(cmd: ModelAction.Model_UpdateDocumentationHome) {
        val value = try {
            val value = cmd.value?.trimToNull()
            if (value == null) null else URI(value).toURL()
        } catch (_: Exception) {
            throw MedatarunException("Should be an URL", StatusCode.BAD_REQUEST)
        }
        dispatch(
            ModelCmd.UpdateModelDocumentationHome(
                modelRef = cmd.modelRef,
                url = value
            )
        )
    }

    fun modelRelease(cmd: ModelAction.Model_Release) {
        dispatch(
            ModelCmd.ModelRelease(
                modelRef = cmd.modelRef,
                version = cmd.value,
            )
        )
    }

    fun modelAddTag(cmd: ModelAction.Model_AddTag) {
        dispatch(
            ModelCmd.UpdateModelTagAdd(
                modelRef = cmd.modelRef,
                tagRef = cmd.tag
            )
        )
    }

    fun modelDeleteTag(cmd: ModelAction.Model_DeleteTag) {
        dispatch(
            ModelCmd.UpdateModelTagDelete(
                modelRef = cmd.modelRef,
                tagRef = cmd.tag
            )
        )
    }

    fun modelDelete(cmd: ModelAction.Model_Delete) {
        dispatch(ModelCmd.DeleteModel(cmd.modelRef))
    }

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    fun typeCreate(cmd: ModelAction.Type_Create) {
        dispatch(
            ModelCmd.CreateType(
                modelRef = cmd.modelRef,
                initializer = ModelTypeInitializer(
                    key = cmd.typeKey,
                    name = cmd.name,
                    description = cmd.description,
                )
            )
        )
    }

    fun typeUpdateKey(cmd: ModelAction.Type_UpdateKey) {
        dispatch(
            ModelCmd.UpdateTypeKey(
                modelRef = cmd.modelRef,
                typeRef = cmd.typeRef,
                value = cmd.value
            )
        )
    }

    fun typeUpdateName(cmd: ModelAction.Type_UpdateName) {
        dispatch(
            ModelCmd.UpdateTypeName(
                modelRef = cmd.modelRef,
                typeRef = cmd.typeRef,
                value = cmd.value
            )
        )
    }

    fun typeUpdateDescription(cmd: ModelAction.Type_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateTypeDescription(
                modelRef = cmd.modelRef,
                typeRef = cmd.typeRef,
                value = cmd.value
            )
        )
    }

    fun typeDelete(cmd: ModelAction.Type_Delete) {
        dispatch(
            ModelCmd.DeleteType(
                modelRef = cmd.modelRef,
                typeRef = cmd.typeRef,
            )
        )
    }
    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------

    fun entityCreate(cmd: ModelAction.Entity_Create) {
        dispatch(
            ModelCmd.CreateEntity(
                modelRef = cmd.modelRef,
                entityInitializer = EntityInitializer(
                    entityKey = cmd.entityKey,
                    name = cmd.name,
                    description = cmd.description,
                    documentationHome = cmd.documentationHome?.let { URI(it).toURL() },
                    identityAttribute = AttributeIdentityInitializer(
                        attributeKey = cmd.identityAttributeKey,
                        type = cmd.identityAttributeType,
                        name = cmd.identityAttributeName,
                        description = null,
                    )
                ),
            )
        )
    }

    fun entityUpdateKey(cmd: ModelAction.Entity_UpdateKey) {
        dispatch(
            ModelCmd.UpdateEntityKey(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                value = cmd.value
            )
        )
    }

    fun entityUpdateName(cmd: ModelAction.Entity_UpdateName) {
        dispatch(
            ModelCmd.UpdateEntityName(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                value = cmd.value
            )
        )
    }

    fun entityUpdateDescription(cmd: ModelAction.Entity_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateEntityDescription(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                value = cmd.value
            )
        )
    }

    fun entityUpdateDocumentationHome(cmd: ModelAction.Entity_UpdateDocumentationHome) {
        val value = try {
            val value = cmd.value?.trimToNull()
            if (value == null) null else URI(value).toURL()
        } catch (_: Exception) {
            throw MedatarunException("Should be an URL", StatusCode.BAD_REQUEST)
        }
        dispatch(
            ModelCmd.UpdateEntityDocumentationHome(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                value = value
            )
        )
    }

    fun entityAddTag(cmd: ModelAction.Entity_AddTag) {
        dispatch(
            ModelCmd.UpdateEntityTagAdd(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                tagRef = cmd.tag
            )
        )
    }

    fun entityDeleteTag(cmd: ModelAction.Entity_DeleteTag) {
        dispatch(
            ModelCmd.UpdateEntityTagDelete(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                tagRef = cmd.tag
            )
        )
    }

    fun entityDelete(cmd: ModelAction.Entity_Delete) {
        dispatch(
            ModelCmd.DeleteEntity(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef
            )
        )
    }

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    fun entityAttributeCreate(cmd: ModelAction.EntityAttribute_Create) {
        dispatch(
            ModelCmd.CreateEntityAttribute(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeInitializer = AttributeInitializer(
                    attributeKey = cmd.attributeKey,
                    type = cmd.type,
                    optional = cmd.optional,
                    name = cmd.name,
                    description = cmd.description,
                )
            )
        )
    }

    fun entityAttributeUpdateKey(cmd: ModelAction.EntityAttribute_UpdateKey) {
        dispatch(
            ModelCmd.UpdateEntityAttributeKey(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                value = cmd.value
            )
        )
    }

    fun entityAttributeUpdateName(cmd: ModelAction.EntityAttribute_UpdateName) {
        dispatch(
            ModelCmd.UpdateEntityAttributeName(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                value = cmd.value
            )
        )
    }


    fun entityAttributeUpdateDescription(cmd: ModelAction.EntityAttribute_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateEntityAttributeDescription(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                value = cmd.value
            )
        )
    }

    fun entityAttributeUpdateType(cmd: ModelAction.EntityAttribute_UpdateType) {
        dispatch(
            ModelCmd.UpdateEntityAttributeType(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                value = cmd.value
            )
        )
    }

    fun entityAttributeUpdateOptional(cmd: ModelAction.EntityAttribute_UpdateOptional) {
        dispatch(
            ModelCmd.UpdateEntityAttributeOptional(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                value = cmd.value
            )
        )
    }


    fun entityAttributeAddTag(cmd: ModelAction.EntityAttribute_AddTag) {
        dispatch(
            ModelCmd.UpdateEntityAttributeTagAdd(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                tagRef = cmd.tag
            )
        )
    }

    fun entityAttributeDeleteTag(cmd: ModelAction.EntityAttribute_DeleteTag) {
        dispatch(
            ModelCmd.UpdateEntityAttributeTagDelete(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                tagRef = cmd.tag
            )
        )
    }

    fun entityAttributeDelete(cmd: ModelAction.EntityAttribute_Delete) {
        dispatch(
            ModelCmd.DeleteEntityAttribute(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
            )
        )
    }
    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    fun relationshipCreate(cmd: ModelAction.Relationship_Create) {
        dispatch(
            ModelCmd.CreateRelationship(
                modelRef = cmd.modelRef,
                initializer = RelationshipInitializer(
                    key = cmd.relationshipKey,
                    name = cmd.name,
                    description = cmd.description,
                    roles = listOf(
                        RelationshipInitializerRole(
                            key = cmd.roleAKey,
                            entityRef = cmd.roleAEntityRef,
                            name = cmd.roleAName,
                            cardinality = cmd.roleACardinality
                        ),
                        RelationshipInitializerRole(
                            key = cmd.roleBKey,
                            entityRef = cmd.roleBEntityRef,
                            name = cmd.roleBName,
                            cardinality = cmd.roleBCardinality
                        )
                    )
                )
            )
        )
    }

    fun relationshipUpdateKey(cmd: ModelAction.Relationship_UpdateKey) {
        dispatch(
            ModelCmd.UpdateRelationshipKey(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                value = cmd.value
            )
        )
    }

    fun relationshipUpdateName(cmd: ModelAction.Relationship_UpdateName) {
        dispatch(
            ModelCmd.UpdateRelationshipName(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                value = cmd.value
            )
        )
    }

    fun relationshipUpdateDescription(cmd: ModelAction.Relationship_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateRelationshipDescription(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                value = cmd.value
            )
        )
    }

    fun relationshipAddTag(cmd: ModelAction.Relationship_AddTag) {
        dispatch(
            ModelCmd.UpdateRelationshipTagAdd(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                tagRef = cmd.tag
            )
        )
    }

    fun relationshipDeleteTag(cmd: ModelAction.Relationship_DeleteTag) {
        dispatch(
            ModelCmd.UpdateRelationshipTagDelete(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                tagRef = cmd.tag
            )
        )
    }

    fun relationshipDelete(cmd: ModelAction.Relationship_Delete) {
        dispatch(
            ModelCmd.DeleteRelationship(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
            )
        )
    }

    fun relationshipRoleCreate(cmd: ModelAction.RelationshipRole_Create) {
        dispatch(
            ModelCmd.CreateRelationshipRole(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                key = cmd.roleKey,
                entityRef = cmd.roleEntityRef,
                name = cmd.roleName,
                cardinality = cmd.roleCardinality
            )
        )
    }

    fun relationshipRoleUpdateKey(cmd: ModelAction.RelationshipRole_UpdateKey) {
        dispatch(
            ModelCmd.UpdateRelationshipRoleKey(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                relationshipRoleRef = cmd.relationshipRoleRef,
                value = cmd.value
            )
        )
    }

    fun relationshipRoleUpdateName(cmd: ModelAction.RelationshipRole_UpdateName) {
        dispatch(
            ModelCmd.UpdateRelationshipRoleName(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                relationshipRoleRef = cmd.relationshipRoleRef,
                value = cmd.value
            )
        )
    }

    fun relationshipRoleUpdateEntity(cmd: ModelAction.RelationshipRole_UpdateEntity) {
        dispatch(
            ModelCmd.UpdateRelationshipRoleEntity(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                relationshipRoleRef = cmd.relationshipRoleRef,
                value = cmd.value
            )
        )
    }

    fun relationshipRoleUpdateCardinality(cmd: ModelAction.RelationshipRole_UpdateCardinality) {
        dispatch(
            ModelCmd.UpdateRelationshipRoleCardinality(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                relationshipRoleRef = cmd.relationshipRoleRef,
                value = cmd.value
            )
        )
    }

    fun relationshipRoleDelete(cmd: ModelAction.RelationshipRole_Delete) {
        dispatch(
            ModelCmd.DeleteRelationshipRole(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                relationshipRoleRef = cmd.relationshipRoleRef
            )
        )
    }

    fun relationshipAttributeCreate(cmd: ModelAction.RelationshipAttribute_Create) {
        dispatch(
            ModelCmd.CreateRelationshipAttribute(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attr = AttributeInitializer(
                    attributeKey = cmd.attributeKey,
                    name = cmd.name,
                    description = cmd.description,
                    type = cmd.type,
                    optional = cmd.optional,
                )
            )
        )
    }

    fun relationshipAttributeUpdateName(cmd: ModelAction.RelationshipAttribute_UpdateName) {
        dispatch(
            ModelCmd.UpdateRelationshipAttributeName(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                value = cmd.value
            )
        )
    }

    fun relationshipAttributeUpdateDescription(cmd: ModelAction.RelationshipAttribute_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateRelationshipAttributeDescription(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                value = cmd.value
            )
        )
    }

    fun relationshipAttributeUpdateKey(cmd: ModelAction.RelationshipAttribute_UpdateKey) {
        dispatch(
            ModelCmd.UpdateRelationshipAttributeKey(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                value = cmd.value
            )
        )
    }

    fun relationshipAttributeUpdateType(cmd: ModelAction.RelationshipAttribute_UpdateType) {
        dispatch(
            ModelCmd.UpdateRelationshipAttributeType(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                value = cmd.value
            )
        )
    }

    fun relationshipAttributeUpdateOptional(cmd: ModelAction.RelationshipAttribute_UpdateOptional) {
        dispatch(
            ModelCmd.UpdateRelationshipAttributeOptional(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                value = cmd.value
            )
        )
    }

    fun relationshipAttributeDelete(cmd: ModelAction.RelationshipAttribute_Delete) {
        dispatch(
            ModelCmd.DeleteRelationshipAttribute(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
            )
        )
    }

    fun relationshipAttributeAddTag(cmd: ModelAction.RelationshipAttribute_AddTag) {
        dispatch(
            ModelCmd.UpdateRelationshipAttributeTagAdd(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                tagRef = cmd.tag
            )
        )
    }

    fun relationshipAttributeDeleteTag(cmd: ModelAction.RelationshipAttribute_DeleteTag) {
        dispatch(
            ModelCmd.UpdateRelationshipAttributeTagDelete(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                tagRef = cmd.tag
            )
        )
    }


    fun historyVersions(action: ModelAction.HistoryVersions): ModelChangeEventListDto {
        val changes = modelQueries.findModelVersions(action.modelRef)
        val appPrincipalResolver = AppPrincipalResolver()
        return toModelChangeEventListDto(changes, appPrincipalResolver)
    }

    fun historyChangesSinceVersion(action: ModelAction.HistoryChangesSinceVersion): ModelChangeEventListDto {
        val changes = modelQueries.findModelChangeEventsSinceVersion(action.modelRef, action.version)
        val appPrincipalResolver = AppPrincipalResolver()
        return toModelChangeEventListDto(changes, appPrincipalResolver)
    }

    @Serializable
    data class ModelListDto(
        val items: List<ModelListItemDto>
    )

    @Serializable
    data class ModelListItemDto(
        val id: String,
        val key: String,
        val name: String?
    )

    @Serializable
    data class ModelCompareDto(
        val scopeApplied: String,
        val left: ModelCompareSideDto,
        val right: ModelCompareSideDto,
        val entries: List<ModelCompareEntryDto>
    )

    @Serializable
    data class ModelCompareSideDto(
        val modelId: String,
        val modelKey: String,
        val modelVersion: String,
        val modelAuthority: String
    )

    @Serializable
    data class ModelCompareEntryDto(
        val status: String,
        val objectType: String,
        val modelKey: String,
        val typeKey: String?,
        val entityKey: String?,
        val relationshipKey: String?,
        val roleKey: String?,
        val attributeKey: String?,
        val left: JsonObject?,
        val right: JsonObject?
    )

    fun modelList(@Suppress("unused") cmd: ModelAction.Model_List): ModelListDto {
        val summaries = modelQueries.findAllModelSummaries(locale)
        val dtos = summaries.map {
            ModelListItemDto(
                id = it.id.value.toString(),
                key = it.key.value,
                name = it.name
            )
        }
        return ModelListDto(dtos)
    }

    private fun toCompareDto(diff: ModelDiff): ModelCompareDto {
        val entries = diff.entries.map { entry -> toCompareEntryDto(entry) }
        return ModelCompareDto(
            scopeApplied = diff.scopeApplied.code,
            left = ModelCompareSideDto(
                modelId = diff.left.modelId.asString(),
                modelKey = diff.left.modelKey.value,
                modelVersion = diff.left.modelVersion.value,
                modelAuthority = diff.left.modelAuthority.code
            ),
            right = ModelCompareSideDto(
                modelId = diff.right.modelId.asString(),
                modelKey = diff.right.modelKey.value,
                modelVersion = diff.right.modelVersion.value,
                modelAuthority = diff.right.modelAuthority.code
            ),
            entries = entries
        )
    }

    private fun toCompareEntryDto(entry: ModelDiffEntry): ModelCompareEntryDto {
        val locationData = toLocationData(entry.location)
        return when (entry) {
            is ModelDiffEntry.Added -> ModelCompareEntryDto(
                status = "ADDED",
                objectType = entry.location.objectType,
                modelKey = locationData.modelKey,
                typeKey = locationData.typeKey,
                entityKey = locationData.entityKey,
                relationshipKey = locationData.relationshipKey,
                roleKey = locationData.roleKey,
                attributeKey = locationData.attributeKey,
                left = null,
                right = toSnapshotJson(entry.right)
            )

            is ModelDiffEntry.Deleted -> ModelCompareEntryDto(
                status = "DELETED",
                objectType = entry.location.objectType,
                modelKey = locationData.modelKey,
                typeKey = locationData.typeKey,
                entityKey = locationData.entityKey,
                relationshipKey = locationData.relationshipKey,
                roleKey = locationData.roleKey,
                attributeKey = locationData.attributeKey,
                left = toSnapshotJson(entry.left),
                right = null
            )

            is ModelDiffEntry.Modified -> ModelCompareEntryDto(
                status = "MODIFIED",
                objectType = entry.location.objectType,
                modelKey = locationData.modelKey,
                typeKey = locationData.typeKey,
                entityKey = locationData.entityKey,
                relationshipKey = locationData.relationshipKey,
                roleKey = locationData.roleKey,
                attributeKey = locationData.attributeKey,
                left = toSnapshotJson(entry.left),
                right = toSnapshotJson(entry.right)
            )
        }
    }

    private data class CompareLocationData(
        val modelKey: String,
        val typeKey: String?,
        val entityKey: String?,
        val relationshipKey: String?,
        val roleKey: String?,
        val attributeKey: String?
    )

    private fun toLocationData(location: ModelDiffLocation): CompareLocationData {
        return when (location) {
            is ModelDiffModelLocation -> CompareLocationData(
                modelKey = location.modelKey.value,
                typeKey = null,
                entityKey = null,
                relationshipKey = null,
                roleKey = null,
                attributeKey = null
            )

            is ModelDiffTypeLocation -> CompareLocationData(
                modelKey = location.modelKey.value,
                typeKey = location.typeKey.value,
                entityKey = null,
                relationshipKey = null,
                roleKey = null,
                attributeKey = null
            )

            is ModelDiffEntityLocation -> CompareLocationData(
                modelKey = location.modelKey.value,
                typeKey = null,
                entityKey = location.entityKey.value,
                relationshipKey = null,
                roleKey = null,
                attributeKey = null
            )

            is ModelDiffEntityAttributeLocation -> CompareLocationData(
                modelKey = location.modelKey.value,
                typeKey = null,
                entityKey = location.entityKey.value,
                relationshipKey = null,
                roleKey = null,
                attributeKey = location.attributeKey.value
            )

            is ModelDiffRelationshipLocation -> CompareLocationData(
                modelKey = location.modelKey.value,
                typeKey = null,
                entityKey = null,
                relationshipKey = location.relationshipKey.value,
                roleKey = null,
                attributeKey = null
            )

            is ModelDiffRelationshipRoleLocation -> CompareLocationData(
                modelKey = location.modelKey.value,
                typeKey = null,
                entityKey = null,
                relationshipKey = location.relationshipKey.value,
                roleKey = location.roleKey.value,
                attributeKey = null
            )

            is ModelDiffRelationshipAttributeLocation -> CompareLocationData(
                modelKey = location.modelKey.value,
                typeKey = null,
                entityKey = null,
                relationshipKey = location.relationshipKey.value,
                roleKey = null,
                attributeKey = location.attributeKey.value
            )
        }
    }

    private fun toSnapshotJson(snapshot: ModelDiffSnapshot): JsonObject {
        return when (snapshot) {
            is ModelDiffModelSnapshot -> buildJsonObject {
                put("objectType", snapshot.objectType)
                put("key", snapshot.key.value)
                putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
                putNullableJsonField(this, "description", toLocalizedMarkdownJson(snapshot.description))
                put("version", snapshot.version.value)
                put("origin", toModelOriginCode(snapshot.origin))
                put("authority", snapshot.authority.code)
                put("documentationHome", snapshot.documentationHome?.toExternalForm())
                putJsonArray("tags") {
                    snapshot.tags.forEach { tag -> add(tag.value.toString()) }
                }
            }

            is ModelDiffTypeSnapshot -> buildJsonObject {
                put("objectType", snapshot.objectType)
                put("key", snapshot.key.value)
                putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
                putNullableJsonField(this, "description", toLocalizedMarkdownJson(snapshot.description))
            }

            is ModelDiffEntitySnapshot -> buildJsonObject {
                put("objectType", snapshot.objectType)
                put("key", snapshot.key.value)
                putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
                putNullableJsonField(this, "description", toLocalizedMarkdownJson(snapshot.description))
                put("identifierAttributeKey", snapshot.identifierAttributeKey.value)
                put("origin", toEntityOriginCode(snapshot.origin))
                put("documentationHome", snapshot.documentationHome?.toExternalForm())
                putJsonArray("tags") {
                    snapshot.tags.forEach { tag -> add(tag.value.toString()) }
                }
            }

            is ModelDiffEntityAttributeSnapshot -> buildJsonObject {
                put("objectType", snapshot.objectType)
                put("key", snapshot.key.value)
                putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
                putNullableJsonField(this, "description", toLocalizedMarkdownJson(snapshot.description))
                put("typeKey", snapshot.typeKey.value)
                put("optional", snapshot.optional)
                putJsonArray("tags") {
                    snapshot.tags.forEach { tag -> add(tag.value.toString()) }
                }
            }

            is ModelDiffRelationshipSnapshot -> buildJsonObject {
                put("objectType", snapshot.objectType)
                put("key", snapshot.key.value)
                putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
                putNullableJsonField(this, "description", toLocalizedMarkdownJson(snapshot.description))
                putJsonArray("tags") {
                    snapshot.tags.forEach { tag -> add(tag.value.toString()) }
                }
            }

            is ModelDiffRelationshipRoleSnapshot -> buildJsonObject {
                put("objectType", snapshot.objectType)
                put("key", snapshot.key.value)
                put("entityKey", snapshot.entityKey.value)
                putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
                put("cardinality", snapshot.cardinality.code)
            }

            is ModelDiffRelationshipAttributeSnapshot -> buildJsonObject {
                put("objectType", snapshot.objectType)
                put("key", snapshot.key.value)
                putNullableJsonField(this, "name", toLocalizedTextJson(snapshot.name))
                putNullableJsonField(this, "description", toLocalizedMarkdownJson(snapshot.description))
                put("typeKey", snapshot.typeKey.value)
                put("optional", snapshot.optional)
                putJsonArray("tags") {
                    snapshot.tags.forEach { tag -> add(tag.value.toString()) }
                }
            }
        }
    }

    private fun putNullableJsonField(
        builder: JsonObjectBuilder,
        key: String,
        value: JsonElement?
    ) {
        if (value == null) {
            builder.put(key, JsonNull)
            return
        }
        builder.put(key, value)
    }

    private fun toLocalizedTextJson(value: LocalizedText?): JsonElement? {
        if (value == null) return null
        if (!value.isLocalized) return JsonPrimitive(value.name)
        val values = value.all()
        return buildJsonObject {
            values.entries.forEach { item ->
                put(item.key, item.value)
            }
        }
    }

    private fun toLocalizedMarkdownJson(value: LocalizedMarkdown?): JsonElement? {
        if (value == null) return null
        if (!value.isLocalized) return JsonPrimitive(value.name)
        val values = value.all()
        return buildJsonObject {
            values.entries.forEach { item ->
                put(item.key, item.value)
            }
        }
    }

    private fun toModelOriginCode(origin: ModelOrigin): String {
        return when (origin) {
            is ModelOrigin.Manual -> "manual"
            is ModelOrigin.Uri -> origin.uri.toString()
        }
    }

    private fun toEntityOriginCode(origin: EntityOrigin): String {
        return when (origin) {
            is EntityOrigin.Manual -> "manual"
            is EntityOrigin.Uri -> origin.uri.toString()
        }
    }

    fun modelExport(cmd: ModelAction.Model_Export): JsonObject {
        val exporters = extensionRegistry.findContributionsFlat(ModelExporter::class)
        val model = modelQueries.findModel(cmd.modelRef)
        val exporter = exporters.firstOrNull() ?: throw ModelExportNoPluginFoundException()
        return exporter.exportJson(model)

    }

    fun modelExportVersion(cmd: ModelAction.Model_Export_Version): JsonObject {
        val exporters = extensionRegistry.findContributionsFlat(ModelExporter::class)
        val model = modelQueries.findModelAtVersion(cmd.modelRef, cmd.version)
        val exporter = exporters.firstOrNull() ?: throw ModelExportNoPluginFoundException()
        return exporter.exportJson(model)

    }

    fun search(cmd: ModelAction.Search): JsonObject {
        val result = modelQueries.search(
            SearchQuery(
                filters = cmd.filters,
                fields = cmd.fields
            )
        )
        return buildJsonObject {
            putJsonArray("items") {
                for (item in result.items) {
                    addJsonObject {
                        put("id", item.id)
                        put("location", createLocation(item.location))

                    }
                }
            }
        }
    }

}

fun createLocation(location: DomainLocation): JsonObject {
    return buildJsonObject {
        put("objectType", location.objectType)
        addLocation(location)
    }
}

fun JsonObjectBuilder.addLocation(location: DomainLocation) {
    when (location) {
        is ModelLocation -> {
            put("modelId", location.id.asString())
            put("modelKey", location.key.value)
            put("modelLabel", location.label)
        }

        is TypeLocation -> {
            addLocation(location.model)
            put("typeId", location.id.asString())
            put("typeKey", location.key.value)
            put("typeLabel", location.label)
        }

        is EntityLocation -> {
            addLocation(location.model)
            put("entityId", location.id.asString())
            put("entityKey", location.key.value)
            put("entityLabel", location.label)
        }

        is EntityAttributeLocation -> {
            addLocation(location.entity.model)
            addLocation(location.entity)
            put("entityAttributeId", location.id.asString())
            put("entityAttributeKey", location.key.value)
            put("entityAttributeLabel", location.label)
        }

        is RelationshipLocation -> {
            addLocation(location.model)
            put("relationshipId", location.id.asString())
            put("relationshipKey", location.key.value)
            put("relationshipLabel", location.label)
        }

        is RelationshipAttributeLocation -> {
            addLocation(location.relationship.model)
            addLocation(location.relationship)
            put("relationshipAttributeId", location.id.asString())
            put("relationshipAttributeKey", location.key.value)
            put("relationshipAttributeLabel", location.label)
        }
    }


}
