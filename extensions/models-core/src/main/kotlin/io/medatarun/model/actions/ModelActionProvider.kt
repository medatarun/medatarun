package io.medatarun.model.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.getService
import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode
import io.medatarun.lang.strings.trimToNull
import io.medatarun.model.domain.*
import io.medatarun.model.domain.diff.*
import io.medatarun.model.domain.search.SearchQuery
import io.medatarun.model.ports.exposed.*
import io.medatarun.model.ports.needs.ModelExporter
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.platform.kernel.ResourceLocator
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*

class ModelActionProvider(private val resourceLocator: ResourceLocator) : ActionProvider<ModelAction> {

    override val actionGroupKey: String = "model"


    /**
     * Returns the list of supported commands. Note that we NEVER return the business model's commands
     * but something mode user-facing so that the model can evolve with preserving maximum compatibility
     * with user-facing actions.
     */
    override fun findCommandClass() = ModelAction::class

    override fun dispatch(cmd: ModelAction, actionCtx: ActionCtx): Any {

        val modelCmds = actionCtx.getService<ModelCmds>()
        val modelQueries = actionCtx.getService<ModelQueries>()

        val locale = Locale.getDefault()

        val handler = ModelActionHandler(modelCmds, modelQueries,  resourceLocator, locale, actionCtx)

        logger.info(cmd.toString())


        val result = when (cmd) {

            // ------------------------------------------------------------------------
            // Models
            // ------------------------------------------------------------------------

            is ModelAction.Import -> handler.modelImport(cmd)
            is ModelAction.Inspect_Json -> handler.modelInspectJson()
            is ModelAction.Compare -> handler.modelCompare(cmd)
            is ModelAction.Search -> handler.search(cmd)

            is ModelAction.Model_List -> handler.modelList(cmd)
            is ModelAction.Model_Export -> handler.modelExport(cmd)

            is ModelAction.Model_Create -> handler.modelCreate(cmd)
            is ModelAction.Model_Copy -> handler.modelCopy(cmd)
            is ModelAction.Model_UpdateKey -> handler.modelUpdateKey(cmd)
            is ModelAction.Model_UpdateName -> handler.modelUpdateName(cmd)
            is ModelAction.Model_UpdateDescription -> handler.modelUpdateDescription(cmd)
            is ModelAction.Model_UpdateAuthority -> handler.modelUpdateAuthority(cmd)
            is ModelAction.Model_UpdateDocumentationHome -> handler.modelUpdateDocumentationHome(cmd)
            is ModelAction.Model_UpdateVersion -> handler.modelUpdateVersion(cmd)
            is ModelAction.Model_AddTag -> handler.modelAddTag(cmd)
            is ModelAction.Model_DeleteTag -> handler.modelDeleteTag(cmd)
            is ModelAction.Model_Delete -> handler.modelDelete(cmd)

            // ------------------------------------------------------------------------
            // Types
            // ------------------------------------------------------------------------

            is ModelAction.Type_Create -> handler.typeCreate(cmd)
            is ModelAction.Type_UpdateName -> handler.typeUpdateName(cmd)
            is ModelAction.Type_UpdateKey -> handler.typeUpdateKey(cmd)
            is ModelAction.Type_UpdateDescription -> handler.typeUpdateDescription(cmd)
            is ModelAction.Type_Delete -> handler.typeDelete(cmd)

            // ------------------------------------------------------------------------
            // Entities
            // ------------------------------------------------------------------------

            is ModelAction.Entity_Create -> handler.entityCreate(cmd)
            is ModelAction.Entity_UpdateKey -> handler.entityUpdateKey(cmd)
            is ModelAction.Entity_UpdateName -> handler.entityUpdateName(cmd)
            is ModelAction.Entity_UpdateDescription -> handler.entityUpdateDescription(cmd)
            is ModelAction.Entity_UpdateDocumentationHome -> handler.entityUpdateDocumentationHome(cmd)
            is ModelAction.Entity_AddTag -> handler.entityAddTag(cmd)
            is ModelAction.Entity_DeleteTag -> handler.entityDeleteTag(cmd)
            is ModelAction.Entity_Delete -> handler.entityDelete(cmd)

            // ------------------------------------------------------------------------
            // Entity attributes
            // ------------------------------------------------------------------------

            is ModelAction.EntityAttribute_Create -> handler.entityAttributeCreate(cmd)
            is ModelAction.EntityAttribute_UpdateKey -> handler.entityAttributeUpdateKey(cmd)
            is ModelAction.EntityAttribute_UpdateName -> handler.entityAttributeUpdateName(cmd)
            is ModelAction.EntityAttribute_UpdateDescription -> handler.entityAttributeUpdateDescription(cmd)
            is ModelAction.EntityAttribute_UpdateType -> handler.entityAttributeUpdateType(cmd)
            is ModelAction.EntityAttribute_UpdateOptional -> handler.entityAttributeUpdateOptional(cmd)
            is ModelAction.EntityAttribute_AddTag -> handler.entityAttributeAddTag(cmd)
            is ModelAction.EntityAttribute_DeleteTag -> handler.entityAttributeDeleteTag(cmd)
            is ModelAction.EntityAttribute_Delete -> handler.entityAttributeDelete(cmd)

            // ------------------------------------------------------------------------
            // Relationships
            // ------------------------------------------------------------------------

            is ModelAction.Relationship_Create -> handler.relationshipCreate(cmd)
            is ModelAction.Relationship_UpdateKey -> handler.relationshipUpdateKey(cmd)
            is ModelAction.Relationship_UpdateName -> handler.relationshipUpdateName(cmd)
            is ModelAction.Relationship_UpdateDescription -> handler.relationshipUpdateDescription(cmd)
            is ModelAction.Relationship_AddTag -> handler.relationshipAddTag(cmd)
            is ModelAction.Relationship_DeleteTag -> handler.relationshipDeleteTag(cmd)
            is ModelAction.Relationship_Delete -> handler.relationshipDelete(cmd)
            is ModelAction.RelationshipRole_Create -> handler.relationshipRoleCreate(cmd)
            is ModelAction.RelationshipRole_UpdateKey -> handler.relationshipRoleUpdateKey(cmd)
            is ModelAction.RelationshipRole_UpdateName -> handler.relationshipRoleUpdateName(cmd)
            is ModelAction.RelationshipRole_UpdateCardinality -> handler.relationshipRoleUpdateCardinality(cmd)
            is ModelAction.RelationshipRole_UpdateEntity -> handler.relationshipRoleUpdateEntity(cmd)
            is ModelAction.RelationshipRole_Delete -> handler.relationshipRoleDelete(cmd)
            is ModelAction.RelationshipAttribute_Create -> handler.relationshipAttributeCreate(cmd)
            is ModelAction.RelationshipAttribute_UpdateKey -> handler.relationshipAttributeUpdateKey(cmd)
            is ModelAction.RelationshipAttribute_UpdateType -> handler.relationshipAttributeUpdateType(cmd)
            is ModelAction.RelationshipAttribute_UpdateOptional -> handler.relationshipAttributeUpdateOptional(cmd)
            is ModelAction.RelationshipAttribute_UpdateName -> handler.relationshipAttributeUpdateName(cmd)
            is ModelAction.RelationshipAttribute_UpdateDescription -> handler.relationshipAttributeUpdateDescription(cmd)
            is ModelAction.RelationshipAttribute_AddTag -> handler.relationshipAttributeAddTag(cmd)
            is ModelAction.RelationshipAttribute_DeleteTag -> handler.relationshipAttributeDeleteTag(cmd)
            is ModelAction.RelationshipAttribute_Delete -> handler.relationshipAttributeDelete(cmd)
        }
        return result
    }


    companion object {
        private val logger = LoggerFactory.getLogger(ModelActionProvider::class.java)
    }
}


class ModelActionHandler(
    private val modelCmds: ModelCmds,
    private val modelQueries: ModelQueries,
    private val resourceLocator: ResourceLocator,
    private val locale: Locale,
    private val actionCtx: ActionCtx
) {
    fun dispatch(businessCmd: ModelCmd) = modelCmds.dispatch(businessCmd)

    fun modelImport(cmd: ModelAction.Import) {
        val contribs = actionCtx.extensionRegistry.findContributionsFlat(ModelImporter::class)
        val resourceLocator = resourceLocator.withPath(cmd.from)
        val contrib = contribs.firstOrNull { contrib ->
            contrib.accept(cmd.from, resourceLocator)
        }
        if (contrib == null) {
            throw ModelImportActionNotFoundException(cmd.from)
        }

        // read model
        val modelData = contrib.toModel(cmd.from, resourceLocator, cmd.modelKey, cmd.modelName)

        // Save imported model
        modelCmds.dispatch(ModelCmd.ImportModel(modelData.model, modelData.tags))
    }



    fun modelInspectJson(): String = ModelInspectJsonAction(modelQueries).process()

    fun modelCompare(cmd: ModelAction.Compare): ModelCompareDto {
        val diff = modelQueries.diff(
            leftModelRef = cmd.leftModelRef,
            rightModelRef = cmd.rightModelRef,
            scope = cmd.scope
        )
        return toCompareDto(diff)
    }

    fun modelCreate(cmd: ModelAction.Model_Create) {
        dispatch(
            ModelCmd.CreateModel(
                modelKey = cmd.modelKey,
                name = cmd.name,
                description = cmd.description,
                version = cmd.version ?: ModelVersion("0.0.1")
            )
        )
    }

    fun modelCopy(cmd: ModelAction.Model_Copy) {
        dispatch(
            ModelCmd.CopyModel(
                modelRef = cmd.modelRef,
                modelNewKey = cmd.modelNewKey
            )
        )
    }

    fun modelUpdateKey(cmd: ModelAction.Model_UpdateKey) {
        dispatch(
            ModelCmd.UpdateModelKey(
                modelRef = cmd.modelRef,
                key = cmd.value
            )
        )
    }

    fun modelUpdateName(cmd: ModelAction.Model_UpdateName) {
        dispatch(
            ModelCmd.UpdateModelName(
                modelRef = cmd.modelRef,
                name = cmd.value,
            )
        )
    }

    fun modelUpdateDescription(cmd: ModelAction.Model_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateModelDescription(
                modelRef = cmd.modelRef,
                description = cmd.value,
            )
        )
    }

    fun modelUpdateAuthority(cmd: ModelAction.Model_UpdateAuthority) {
        dispatch(
            ModelCmd.UpdateModelAuthority(
                modelRef = cmd.modelRef,
                authority = cmd.value
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

    fun modelUpdateVersion(cmd: ModelAction.Model_UpdateVersion) {
        dispatch(
            ModelCmd.UpdateModelVersion(
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
    ) {
        fun entriesByObjectType(objectType: String): List<ModelCompareEntryDto> {
            return entries.filter { it.objectType == objectType }
        }
    }

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
    ) {
        fun isModified(): Boolean {
            return status == "MODIFIED"
        }

        fun isAdded(): Boolean {
            return status == "ADDED"
        }

        fun isDeleted(): Boolean {
            return status == "DELETED"
        }
    }

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
        val exporters = actionCtx.extensionRegistry.findContributionsFlat(ModelExporter::class)
        val model = modelQueries.findModel(cmd.modelRef)
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
