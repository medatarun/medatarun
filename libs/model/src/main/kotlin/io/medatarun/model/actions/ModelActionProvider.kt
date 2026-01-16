package io.medatarun.model.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.getService
import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.*
import io.medatarun.platform.kernel.ResourceLocator
import org.slf4j.LoggerFactory
import java.net.URI

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

        val handler = ModelActionHandler(modelCmds, modelQueries, modelHumanPrinter, resourceLocator)

        logger.info(cmd.toString())


        val result = when (cmd) {

            // ------------------------------------------------------------------------
            // Models
            // ------------------------------------------------------------------------

            is ModelAction.Import -> handler.modelImport(actionCtx, cmd)
            is ModelAction.Inspect_Human -> handler.modelInspectHuman()
            is ModelAction.Inspect_Json -> handler.modelInspectJson()

            is ModelAction.Model_Create -> handler.modelCreate(cmd)
            is ModelAction.Model_Copy -> handler.modelCopy(cmd)
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
            is ModelAction.Entity_UpdateId -> handler.entityUpdateId(cmd)
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
            is ModelAction.Relationship_Update -> handler.relationshipUpdate(cmd)
            is ModelAction.Relationship_AddTag -> handler.relationshipAddTag(cmd)
            is ModelAction.Relationship_DeleteTag -> handler.relationshipDeleteTag(cmd)
            is ModelAction.Relationship_Delete -> handler.relationshipDelete(cmd)
            is ModelAction.RelationshipAttribute_Create -> handler.relationshipAttributeCreate(cmd)
            is ModelAction.RelationshipAttribute_Update -> handler.relationshipAttributeUpdate(cmd)
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
    private val resourceLocator: ResourceLocator
) {
    fun dispatch(businessCmd: ModelCmd) = modelCmds.dispatch(businessCmd)

    fun modelImport(actionCtx: ActionCtx, rc: ModelAction.Import) {
        ModelImportAction(actionCtx.extensionRegistry, modelCmds, resourceLocator)
            .process(rc)
    }

    fun modelInspectHuman(): String = ModelInspectAction(modelQueries, modelHumanPrinter).process()

    fun modelInspectJson(): String = ModelInspectJsonAction(modelQueries).process()

    fun modelCreate(cmd: ModelAction.Model_Create) {
        dispatch(
            ModelCmd.CreateModel(
                modelKey = cmd.modelKey,
                name = LocalizedTextNotLocalized(cmd.name),
                description = cmd.description?.let { LocalizedTextNotLocalized(it) },
                version = cmd.version ?: ModelVersion("0.0.1")
            )
        )
    }

    fun modelCopy(cmd: ModelAction.Model_Copy) {
        dispatch(
            ModelCmd.CopyModel(
                modelKey = cmd.modelKey,
                modelNewKey = cmd.modelNewKey
            )
        )
    }

    fun modelUpdateName(cmd: ModelAction.Model_UpdateName) {
        dispatch(
            ModelCmd.UpdateModelName(
                modelKey = cmd.modelKey,
                name = LocalizedTextNotLocalized(cmd.name),
            )
        )
    }

    fun modelUpdateDescription(cmd: ModelAction.Model_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateModelDescription(
                modelKey = cmd.modelKey,
                description = cmd.description?.let { LocalizedTextNotLocalized(it) },
            )
        )
    }

    fun modelUpdateVersion(cmd: ModelAction.Model_UpdateVersion) {
        dispatch(
            ModelCmd.UpdateModelVersion(
                modelKey = cmd.modelKey,
                version = cmd.version,
            )
        )
    }

    fun modelAddTag(cmd: ModelAction.Model_AddTag) {
        dispatch(
            ModelCmd.UpdateModelHashtagAdd(
                modelKey = cmd.modelKey,
                hashtag = cmd.tag
            )
        )
    }

    fun modelDeleteTag(cmd: ModelAction.Model_DeleteTag) {
        dispatch(
            ModelCmd.UpdateModelHashtagDelete(
                modelKey = cmd.modelKey,
                hashtag = cmd.tag
            )
        )
    }

    fun modelDelete(cmd: ModelAction.Model_Delete) {
        dispatch(ModelCmd.DeleteModel(cmd.modelKey))
    }

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    fun typeCreate(cmd: ModelAction.Type_Create) {
        dispatch(
            ModelCmd.CreateType(
                modelKey = cmd.modelKey,
                initializer = ModelTypeInitializer(
                    id = cmd.typeKey,
                    name = cmd.name?.let { LocalizedTextNotLocalized(it) },
                    description = cmd.description?.let { LocalizedTextNotLocalized(it) },
                )
            )
        )
    }

    fun typeUpdateName(cmd: ModelAction.Type_UpdateName) {
        dispatch(
            ModelCmd.UpdateType(
                modelKey = cmd.modelKey,
                typeId = cmd.typeKey,
                cmd = ModelTypeUpdateCmd.Name(cmd.name?.let { LocalizedTextNotLocalized(it) })
            )
        )
    }

    fun typeUpdateDescription(cmd: ModelAction.Type_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateType(
                modelKey = cmd.modelKey,
                typeId = cmd.typeKey,
                cmd = ModelTypeUpdateCmd.Description(cmd.description?.let { LocalizedTextNotLocalized(it) })
            )
        )
    }

    fun typeDelete(cmd: ModelAction.Type_Delete) {
        dispatch(
            ModelCmd.DeleteType(
                modelKey = cmd.modelKey,
                typeId = cmd.typeKey,
            )
        )
    }
    // ------------------------------------------------------------------------
    // Entities
    // ------------------------------------------------------------------------

    fun entityCreate(cmd: ModelAction.Entity_Create) {
        dispatch(
            ModelCmd.CreateEntityDef(
                modelKey = cmd.modelKey,
                entityDefInitializer = EntityDefInitializer(
                    entityKey = cmd.entityKey,
                    name = cmd.name?.let { LocalizedTextNotLocalized(it) },
                    description = cmd.description?.let { LocalizedTextNotLocalized(it) },
                    documentationHome = cmd.documentationHome?.let { URI(it).toURL() },
                    identityAttribute = AttributeDefIdentityInitializer(
                        attributeKey = cmd.identityAttributeKey,
                        type = cmd.identityAttributeType,
                        name = cmd.name?.let { LocalizedTextNotLocalized(it) },
                        description = cmd.description?.let { LocalizedTextNotLocalized(it) },
                    )
                ),
            )
        )
    }

    fun entityUpdateId(cmd: ModelAction.Entity_UpdateId) {
        dispatch(
            ModelCmd.UpdateEntityDef(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                cmd = EntityDefUpdateCmd.Id(EntityKey(cmd.value))
            )
        )
    }

    fun entityUpdateName(cmd: ModelAction.Entity_UpdateName) {
        dispatch(
            ModelCmd.UpdateEntityDef(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                cmd = EntityDefUpdateCmd.Name(cmd.value?.let { LocalizedTextNotLocalized(it) })
            )
        )
    }

    fun entityUpdateDescription(cmd: ModelAction.Entity_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateEntityDef(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                cmd = EntityDefUpdateCmd.Description(cmd.value?.let { LocalizedTextNotLocalized(it) })
            )
        )
    }

    fun entityAddTag(cmd: ModelAction.Entity_AddTag) {
        dispatch(
            ModelCmd.UpdateEntityDefHashtagAdd(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                hashtag = cmd.tag
            )
        )
    }

    fun entityDeleteTag(cmd: ModelAction.Entity_DeleteTag) {
        dispatch(
            ModelCmd.UpdateEntityDefHashtagDelete(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                hashtag = cmd.tag
            )
        )
    }

    fun entityDelete(cmd: ModelAction.Entity_Delete) {
        dispatch(
            ModelCmd.DeleteEntityDef(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey
            )
        )
    }

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    fun entityAttributeCreate(cmd: ModelAction.EntityAttribute_Create) {
        dispatch(
            ModelCmd.CreateEntityDefAttributeDef(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                attributeDefInitializer = AttributeDefInitializer(
                    attributeKey = cmd.attributeKey,
                    type = cmd.type,
                    optional = cmd.optional,
                    name = cmd.name?.let { LocalizedTextNotLocalized(it) },
                    description = cmd.description?.let { LocalizedTextNotLocalized(it) },
                )
            )
        )
    }

    fun entityAttributeUpdateId(cmd: ModelAction.EntityAttribute_UpdateId) {
        dispatch(
            ModelCmd.UpdateEntityDefAttributeDef(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                attributeKey = cmd.attributeKey,
                cmd = AttributeDefUpdateCmd.Id(AttributeKey(cmd.value))
            )
        )
    }

    fun entityAttributeUpdateName(cmd: ModelAction.EntityAttribute_UpdateName) {
        dispatch(
            ModelCmd.UpdateEntityDefAttributeDef(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                attributeKey = cmd.attributeKey,
                cmd = AttributeDefUpdateCmd.Name(cmd.value?.let { LocalizedTextNotLocalized(it) })
            )
        )
    }


    fun entityAttributeUpdateDescription(cmd: ModelAction.EntityAttribute_UpdateDescription) {
        dispatch(
            ModelCmd.UpdateEntityDefAttributeDef(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                attributeKey = cmd.attributeKey,
                cmd = AttributeDefUpdateCmd.Description(cmd.value?.let { LocalizedTextNotLocalized(it) })
            )
        )
    }

    fun entityAttributeUpdateType(cmd: ModelAction.EntityAttribute_UpdateType) {
        dispatch(
            ModelCmd.UpdateEntityDefAttributeDef(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                attributeKey = cmd.attributeKey,
                cmd = AttributeDefUpdateCmd.Type(TypeKey(cmd.value))
            )
        )
    }

    fun entityAttributeUpdateOptional(cmd: ModelAction.EntityAttribute_UpdateOptional) {
        dispatch(
            ModelCmd.UpdateEntityDefAttributeDef(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                attributeKey = cmd.attributeKey,
                cmd = AttributeDefUpdateCmd.Optional(cmd.value)
            )
        )
    }


    fun entityAttributeAddTag(cmd: ModelAction.EntityAttribute_AddTag) {
        dispatch(
            ModelCmd.UpdateEntityDefAttributeDefHashtagAdd(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                attributeKey = cmd.attributeKey,
                hashtag = cmd.tag
            )
        )
    }

    fun entityAttributeDeleteTag(cmd: ModelAction.EntityAttribute_DeleteTag) {
        dispatch(
            ModelCmd.UpdateEntityDefAttributeDefHashtagDelete(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                attributeKey = cmd.attributeKey,
                hashtag = cmd.tag
            )
        )
    }

    fun entityAttributeDelete(cmd: ModelAction.EntityAttribute_Delete) {
        dispatch(
            ModelCmd.DeleteEntityDefAttributeDef(
                modelKey = cmd.modelKey,
                entityKey = cmd.entityKey,
                attributeKey = cmd.attributeKey
            )
        )
    }
    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    fun relationshipCreate(cmd: ModelAction.Relationship_Create) {
        dispatch(
            ModelCmd.CreateRelationshipDef(
                modelKey = cmd.modelKey,
                initializer = cmd.initializer
            )
        )
    }

    fun relationshipUpdate(cmd: ModelAction.Relationship_Update) {
        dispatch(
            ModelCmd.UpdateRelationshipDef(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                cmd = cmd.cmd
            )
        )
    }

    fun relationshipAddTag(cmd: ModelAction.Relationship_AddTag) {
        dispatch(
            ModelCmd.UpdateRelationshipDefHashtagAdd(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                hashtag = cmd.tag
            )
        )
    }

    fun relationshipDeleteTag(cmd: ModelAction.Relationship_DeleteTag) {
        dispatch(
            ModelCmd.UpdateRelationshipDefHashtagDelete(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                hashtag = cmd.tag
            )
        )
    }

    fun relationshipDelete(cmd: ModelAction.Relationship_Delete) {
        dispatch(
            ModelCmd.DeleteRelationshipDef(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey
            )
        )
    }

    fun relationshipAttributeCreate(cmd: ModelAction.RelationshipAttribute_Create) {
        dispatch(
            ModelCmd.CreateRelationshipAttributeDef(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                attr = cmd.attr
            )
        )
    }

    fun relationshipAttributeUpdate(cmd: ModelAction.RelationshipAttribute_Update) {
        dispatch(
            ModelCmd.UpdateRelationshipAttributeDef(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                attributeKey = cmd.attributeKey,
                cmd = cmd.cmd
            )
        )
    }

    fun relationshipAttributeDelete(cmd: ModelAction.RelationshipAttribute_Delete) {
        dispatch(
            ModelCmd.DeleteRelationshipAttributeDef(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                attributeKey = cmd.attributeKey,
            )
        )
    }

    fun relationshipAttributeAddTag(cmd: ModelAction.RelationshipAttribute_AddTag) {
        dispatch(
            ModelCmd.UpdateRelationshipAttributeDefHashtagAdd(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                attributeKey = cmd.attributeKey,
                hashtag = cmd.tag
            )
        )
    }

    fun relationshipAttributeDeleteTag(cmd: ModelAction.RelationshipAttribute_DeleteTag) {
        dispatch(
            ModelCmd.UpdateRelationshipAttributeDefHashtagDelete(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                attributeKey = cmd.attributeKey,
                hashtag = cmd.tag
            )
        )
    }
}
