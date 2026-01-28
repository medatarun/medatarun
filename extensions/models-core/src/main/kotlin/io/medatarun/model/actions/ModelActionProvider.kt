package io.medatarun.model.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.getService
import io.medatarun.model.domain.ModelExportNoPluginFoundException
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.ports.exposed.*
import io.medatarun.model.ports.needs.ModelExporter
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.platform.kernel.ResourceLocator
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*

class ModelActionProvider(private val resourceLocator: ResourceLocator) : ActionProvider<ModelAction> {

    override val actionGroupKey: String = "model"


    /**
     * Returns the list of supported commands. Note that we NEVER return the business model's commands
     * but something mode user-facing so that the model can evolve with preserving maximum compatibility
     * with user facing actions.
     */
    override fun findCommandClass() = ModelAction::class

    override fun dispatch(cmd: ModelAction, actionCtx: ActionCtx): Any {

        val modelCmds = actionCtx.getService<ModelCmds>()
        val modelQueries = actionCtx.getService<ModelQueries>()
        val modelHumanPrinter = actionCtx.getService<ModelHumanPrinter>()
        val locale = Locale.getDefault()

        val handler = ModelActionHandler(modelCmds, modelQueries, modelHumanPrinter, resourceLocator, locale, actionCtx)

        logger.info(cmd.toString())


        val result = when (cmd) {

            // ------------------------------------------------------------------------
            // Models
            // ------------------------------------------------------------------------

            is ModelAction.Import -> handler.modelImport(cmd)
            is ModelAction.Inspect_Human -> handler.modelInspectHuman()
            is ModelAction.Inspect_Json -> handler.modelInspectJson()

            is ModelAction.Model_List -> handler.modelList(cmd)
            is ModelAction.Model_Export -> handler.modelExport(cmd)

            is ModelAction.Model_Create -> handler.modelCreate(cmd)
            is ModelAction.Model_Copy -> handler.modelCopy(cmd)
            is ModelAction.Model_UpdateKey -> handler.modelUpdateKey(cmd)
            is ModelAction.Model_UpdateName -> handler.modelUpdateName(cmd)
            is ModelAction.Model_UpdateDescription -> handler.modelUpdateDescription(cmd)
            is ModelAction.Model_UpdateVersion -> handler.modelUpdateVersion(cmd)
            is ModelAction.Model_AddTag -> handler.modelAddTag(cmd)
            is ModelAction.Model_DeleteTag -> handler.modelDeleteTag(cmd)
            is ModelAction.Model_Delete -> handler.modelDelete(cmd)

            // ------------------------------------------------------------------------
            // Types
            // ------------------------------------------------------------------------

            is ModelAction.Type_Create -> handler.typeCreate(cmd)
            is ModelAction.Type_UpdateName -> handler.typeUpdateName(cmd)
            is ModelAction.Type_UpdateDescription -> handler.typeUpdateDescription(cmd)
            is ModelAction.Type_Delete -> handler.typeDelete(cmd)

            // ------------------------------------------------------------------------
            // Entities
            // ------------------------------------------------------------------------

            is ModelAction.Entity_Create -> handler.entityCreate(cmd)
            is ModelAction.Entity_UpdateKey -> handler.entityUpdateId(cmd)
            is ModelAction.Entity_UpdateName -> handler.entityUpdateName(cmd)
            is ModelAction.Entity_UpdateDescription -> handler.entityUpdateDescription(cmd)
            is ModelAction.Entity_AddTag -> handler.entityAddTag(cmd)
            is ModelAction.Entity_DeleteTag -> handler.entityDeleteTag(cmd)
            is ModelAction.Entity_Delete -> handler.entityDelete(cmd)

            // ------------------------------------------------------------------------
            // Entity attributes
            // ------------------------------------------------------------------------

            is ModelAction.EntityAttribute_Create -> handler.entityAttributeCreate(cmd)
            is ModelAction.EntityAttribute_UpdateId -> handler.entityAttributeUpdateId(cmd)
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
            is ModelAction.RelationshipAttribute_UpdateKey -> handler.relationshipAttributeUpdateId(cmd)
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
    private val modelHumanPrinter: ModelHumanPrinter,
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
        val model = contrib.toModel(cmd.from, resourceLocator, cmd.modelKey, cmd.modelName)
        modelCmds.dispatch(ModelCmd.ImportModel(model))
    }

    fun modelInspectHuman(): String = ModelInspectAction(modelQueries, modelHumanPrinter).process()

    fun modelInspectJson(): String = ModelInspectJsonAction(modelQueries).process()

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
        TODO("Not yet implemented")
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
            ModelCmd.UpdateModelHashtagAdd(
                modelRef = cmd.modelRef,
                hashtag = cmd.tag
            )
        )
    }

    fun modelDeleteTag(cmd: ModelAction.Model_DeleteTag) {
        dispatch(
            ModelCmd.UpdateModelHashtagDelete(
                modelRef = cmd.modelRef,
                hashtag = cmd.tag
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
                    id = cmd.typeKey,
                    name = cmd.name,
                    description = cmd.description,
                )
            )
        )
    }

    fun typeUpdateName(cmd: ModelAction.Type_UpdateName) {
        dispatch(
            ModelCmd.UpdateType(
                modelRef = cmd.modelRef,
                typeRef = cmd.typeRef,
                cmd = ModelTypeUpdateCmd.Name(cmd.name)
            )
        )
    }

    fun typeUpdateDescription(cmd: ModelAction.Type_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateType(
                modelRef = cmd.modelRef,
                typeRef = cmd.typeRef,
                cmd = ModelTypeUpdateCmd.Description(cmd.description)
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
                        name = cmd.name,
                        description = cmd.description,
                    )
                ),
            )
        )
    }

    fun entityUpdateId(cmd: ModelAction.Entity_UpdateKey) {
        dispatch(
            ModelCmd.UpdateEntity(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                cmd = EntityUpdateCmd.Key(cmd.value)
            )
        )
    }

    fun entityUpdateName(cmd: ModelAction.Entity_UpdateName) {
        dispatch(
            ModelCmd.UpdateEntity(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                cmd = EntityUpdateCmd.Name(cmd.value)
            )
        )
    }

    fun entityUpdateDescription(cmd: ModelAction.Entity_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateEntity(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                cmd = EntityUpdateCmd.Description(cmd.value)
            )
        )
    }

    fun entityAddTag(cmd: ModelAction.Entity_AddTag) {
        dispatch(
            ModelCmd.UpdateEntityHashtagAdd(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                hashtag = cmd.tag
            )
        )
    }

    fun entityDeleteTag(cmd: ModelAction.Entity_DeleteTag) {
        dispatch(
            ModelCmd.UpdateEntityHashtagDelete(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                hashtag = cmd.tag
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

    fun entityAttributeUpdateId(cmd: ModelAction.EntityAttribute_UpdateId) {
        dispatch(
            ModelCmd.UpdateEntityAttribute(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                cmd = AttributeUpdateCmd.Key(cmd.value)
            )
        )
    }

    fun entityAttributeUpdateName(cmd: ModelAction.EntityAttribute_UpdateName) {
        dispatch(
            ModelCmd.UpdateEntityAttribute(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                cmd = AttributeUpdateCmd.Name(cmd.value)
            )
        )
    }


    fun entityAttributeUpdateDescription(cmd: ModelAction.EntityAttribute_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateEntityAttribute(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                cmd = AttributeUpdateCmd.Description(cmd.value)
            )
        )
    }

    fun entityAttributeUpdateType(cmd: ModelAction.EntityAttribute_UpdateType) {
        dispatch(
            ModelCmd.UpdateEntityAttribute(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                cmd = AttributeUpdateCmd.Type(cmd.value)
            )
        )
    }

    fun entityAttributeUpdateOptional(cmd: ModelAction.EntityAttribute_UpdateOptional) {
        dispatch(
            ModelCmd.UpdateEntityAttribute(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                cmd = AttributeUpdateCmd.Optional(cmd.value)
            )
        )
    }


    fun entityAttributeAddTag(cmd: ModelAction.EntityAttribute_AddTag) {
        dispatch(
            ModelCmd.UpdateEntityAttributeHashtagAdd(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                hashtag = cmd.tag
            )
        )
    }

    fun entityAttributeDeleteTag(cmd: ModelAction.EntityAttribute_DeleteTag) {
        dispatch(
            ModelCmd.UpdateEntityAttributeHashtagDelete(
                modelRef = cmd.modelRef,
                entityRef = cmd.entityRef,
                attributeRef = cmd.attributeRef,
                hashtag = cmd.tag
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
            ModelCmd.UpdateRelationship(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                cmd = RelationshipUpdateCmd.Key(cmd.value)
            )
        )
    }

    fun relationshipUpdateName(cmd: ModelAction.Relationship_UpdateName) {
        dispatch(
            ModelCmd.UpdateRelationship(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                cmd = RelationshipUpdateCmd.Name(cmd.value)
            )
        )
    }

    fun relationshipUpdateDescription(cmd: ModelAction.Relationship_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateRelationship(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                cmd = RelationshipUpdateCmd.Description(cmd.value)
            )
        )
    }

    fun relationshipAddTag(cmd: ModelAction.Relationship_AddTag) {
        dispatch(
            ModelCmd.UpdateRelationshipHashtagAdd(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                hashtag = cmd.tag
            )
        )
    }

    fun relationshipDeleteTag(cmd: ModelAction.Relationship_DeleteTag) {
        dispatch(
            ModelCmd.UpdateRelationshipHashtagDelete(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                hashtag = cmd.tag
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
        TODO("Not yet implemented")
    }

    fun relationshipRoleUpdateKey(cmd: ModelAction.RelationshipRole_UpdateKey) {
        TODO("Not yet implemented")
    }

    fun relationshipRoleUpdateName(cmd: ModelAction.RelationshipRole_UpdateName) {
        TODO("Not yet implemented")
    }

    fun relationshipRoleUpdateEntity(cmd: ModelAction.RelationshipRole_UpdateEntity) {
        TODO("Not yet implemented")
    }

    fun relationshipRoleUpdateCardinality(cmd: ModelAction.RelationshipRole_UpdateCardinality) {
        TODO("Not yet implemented")
    }

    fun relationshipRoleDelete(cmd: ModelAction.RelationshipRole_Delete) {
        TODO("Not yet implemented")
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
            ModelCmd.UpdateRelationshipAttribute(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                cmd = AttributeUpdateCmd.Name(cmd.value)
            )
        )
    }

    fun relationshipAttributeUpdateDescription(cmd: ModelAction.RelationshipAttribute_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateRelationshipAttribute(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                cmd = AttributeUpdateCmd.Description(cmd.value)
            )
        )
    }

    fun relationshipAttributeUpdateId(cmd: ModelAction.RelationshipAttribute_UpdateKey) {
        dispatch(
            ModelCmd.UpdateRelationshipAttribute(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                cmd = AttributeUpdateCmd.Key(cmd.value)
            )
        )
    }

    fun relationshipAttributeUpdateType(cmd: ModelAction.RelationshipAttribute_UpdateType) {
        dispatch(
            ModelCmd.UpdateRelationshipAttribute(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                cmd = AttributeUpdateCmd.Type(cmd.value)
            )
        )
    }

    fun relationshipAttributeUpdateOptional(cmd: ModelAction.RelationshipAttribute_UpdateOptional) {
        dispatch(
            ModelCmd.UpdateRelationshipAttribute(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                cmd = AttributeUpdateCmd.Optional(cmd.value)
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
            ModelCmd.UpdateRelationshipAttributeHashtagAdd(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                hashtag = cmd.tag
            )
        )
    }

    fun relationshipAttributeDeleteTag(cmd: ModelAction.RelationshipAttribute_DeleteTag) {
        dispatch(
            ModelCmd.UpdateRelationshipAttributeHashtagDelete(
                modelRef = cmd.modelRef,
                relationshipRef = cmd.relationshipRef,
                attributeRef = cmd.attributeRef,
                hashtag = cmd.tag
            )
        )
    }

    @Serializable
    data class ModelListItemDto(
        val id: String,
        val key: String,
        val name: String?
    )

    fun modelList(cmd: ModelAction.Model_List): List<ModelListItemDto> {
        val summaries = modelQueries.findAllModelSummaries(locale)
        return summaries.map {
            ModelListItemDto(
                id = it.id.value.toString(),
                key = it.key.value,
                name = it.name
            )
        }
    }

    fun modelExport(cmd: ModelAction.Model_Export): JsonObject {
        val exporters = actionCtx.extensionRegistry.findContributionsFlat(ModelExporter::class)
        val model = modelQueries.findModel(cmd.modelRef)
        val exporter = exporters.firstOrNull() ?: throw ModelExportNoPluginFoundException()
        return exporter.exportJson(model)

    }
}
