package io.medatarun.model.actions

import io.medatarun.actions.adapters.ActionTraceabilityRecord
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode
import io.medatarun.lang.strings.trimToNull
import io.medatarun.model.actions.compare.ModelCompareDto
import io.medatarun.model.actions.compare.ModelCompareDtoAdapters
import io.medatarun.model.actions.history.ModelChangeEventDisplayResolver
import io.medatarun.model.actions.history.ModelChangeEventListDto
import io.medatarun.model.actions.history.toModelChangeEventListDto
import io.medatarun.model.actions.list.ModelListDto
import io.medatarun.model.actions.list.ModelListDtoAdapters
import io.medatarun.model.actions.search.ModelSearchDtoAdapters
import io.medatarun.model.domain.ModelActionNotAuthenticatedException
import io.medatarun.model.domain.ModelExportNoPluginFoundException
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.domain.search.SearchQuery
import io.medatarun.model.ports.exposed.*
import io.medatarun.model.ports.needs.ModelExporter
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.ResourceLocator
import io.medatarun.security.AppActorResolver
import io.medatarun.tags.core.domain.TagQueries
import kotlinx.serialization.json.JsonObject
import java.net.URI
import java.util.*

class ModelActionHandler(
    private val modelCmds: ModelCmds,
    private val modelQueries: ModelQueries,
    private val tagQueries: TagQueries,
    private val resourceLocator: ResourceLocator,
    private val locale: Locale,
    private val actionCtx: ActionCtx,
    private val extensionRegistry: ExtensionRegistry,
    private val actorResolver: AppActorResolver
) {
    val displayResolver = ModelChangeEventDisplayResolver(tagQueries)

    fun dispatch(businessCmd: ModelCmd) {
        val principal = actionCtx.principal.principal ?: throw ModelActionNotAuthenticatedException()
        modelCmds.dispatch(
            ModelCmdEnveloppe(
                traceabilityRecord = ActionTraceabilityRecord(actionCtx.actionInstanceId, principal.id),
                cmd = businessCmd
            )
        )
    }

    fun maintenanceRebuildCaches() {
        modelCmds.maintenanceRebuildCaches()
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
            leftModelVersion = action.leftModelVersion,
            rightModelRef = action.rightModelRef,
            rightModelVersion = action.rightModelVersion,
            scope = action.scope
        )
        return ModelCompareDtoAdapters.toCompareDto(diff)
    }

    fun modelCreate(action: ModelAction.Model_Create) {
        dispatch(
            ModelCmd.CreateModel(
                modelKey = action.key,
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

        return toModelChangeEventListDto(changes, actorResolver, displayResolver)
    }

    fun historyChangesSinceVersion(action: ModelAction.HistoryVersionChanges): ModelChangeEventListDto {
        val changes = if (action.version == null) {
            modelQueries.findModelChangeEventsSinceLastReleaseEvent(action.modelRef)
        } else {
            modelQueries.findModelChangeEventsInVersion(action.modelRef, action.version)
        }

        return toModelChangeEventListDto(changes, actorResolver, displayResolver)
    }

    fun modelList(@Suppress("unused") cmd: ModelAction.Model_List): ModelListDto {
        val summaries = modelQueries.findAllModelSummaries(locale)
        return ModelListDtoAdapters.toModelListDto(summaries)
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
        val result = modelQueries.search(SearchQuery(filters = cmd.filters, fields = cmd.fields))
        return ModelSearchDtoAdapters.toModelSearchResults(result)
    }

}
