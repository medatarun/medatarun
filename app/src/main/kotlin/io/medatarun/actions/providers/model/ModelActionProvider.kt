package io.medatarun.actions.providers.model

import io.medatarun.actions.runtime.ActionCtx
import io.medatarun.actions.runtime.ActionProvider
import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.*
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.file.FileSystems

class ModelActionProvider() : ActionProvider<ModelAction> {

    /**
     * Returns the list of supported commands. Note that we NEVER return the business model's commands
     * but something mode user-facing so that the model can evolve with preserving maximum compatibility
     * with user facing actions.
     */
    override fun findCommandClass() = ModelAction::class

    override fun dispatch(cmd: ModelAction, actionCtx: ActionCtx): Any? {
        val rc = cmd as ModelAction

        fun dispatch(businessCmd: ModelCmd) = actionCtx.modelCmds.dispatch(businessCmd)

        logger.info(rc.toString())

        val result = when (rc) {

            // ------------------------------------------------------------------------
            // Models
            // ------------------------------------------------------------------------

            is ModelAction.Import -> ModelImportAction(actionCtx, FileSystems.getDefault()).process(rc)
            is ModelAction.Inspect -> ModelInspectAction(actionCtx).process()
            is ModelAction.InspectJson -> ModelInspectJsonAction(actionCtx).process()
            is ModelAction.CreateModel -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.CreateModel(
                        id = ModelId(rc.id),
                        name = LocalizedTextNotLocalized(rc.name),
                        description = rc.description?.let { LocalizedTextNotLocalized(it) },
                        version = rc.version ?: ModelVersion("1.0.0")
                    )
                )
            }

            is ModelAction.UpdateModelName -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.UpdateModelName(
                        modelId = ModelId(rc.id),
                        name = LocalizedTextNotLocalized(rc.name),
                    )
                )
            }

            is ModelAction.UpdateModelDescription -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.UpdateModelDescription(
                        modelId = ModelId(rc.id),
                        description = rc.description?.let { LocalizedTextNotLocalized(it) },
                    )
                )
            }

            is ModelAction.UpdateModelVersion -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.UpdateModelVersion(
                        modelId = ModelId(rc.id),
                        version = ModelVersion(rc.version),
                    )
                )
            }

            is ModelAction.DeleteModel -> {
                actionCtx.modelCmds.dispatch(ModelCmd.DeleteModel(ModelId(rc.id)))
            }

            // ------------------------------------------------------------------------
            // Types
            // ------------------------------------------------------------------------

            is ModelAction.CreateType -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.CreateType(
                        modelId = ModelId(rc.modelId),
                        initializer = ModelTypeInitializer(
                            id = ModelTypeId(rc.typeId),
                            name = rc.name?.let { LocalizedTextNotLocalized(it) },
                            description = rc.description?.let { LocalizedTextNotLocalized(it) },
                        )
                    )
                )
            }

            is ModelAction.UpdateTypeName -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.UpdateType(
                        modelId = ModelId(rc.modelId),
                        typeId = ModelTypeId(rc.typeId),
                        cmd = ModelTypeUpdateCmd.Name(rc.name?.let { LocalizedTextNotLocalized(it) })
                    )
                )
            }

            is ModelAction.UpdateTypeDescription -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.UpdateType(
                        modelId = ModelId(rc.modelId),
                        typeId = ModelTypeId(rc.typeId),
                        cmd = ModelTypeUpdateCmd.Description(rc.description?.let { LocalizedTextNotLocalized(it) })
                    )
                )
            }

            is ModelAction.DeleteType -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.DeleteType(
                        modelId = ModelId(rc.modelId),
                        typeId = ModelTypeId(rc.typeId),
                    )
                )
            }

            // ------------------------------------------------------------------------
            // Entities
            // ------------------------------------------------------------------------

            is ModelAction.CreateEntity -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.CreateEntityDef(
                        modelId = ModelId(rc.modelId),
                        entityDefInitializer = EntityDefInitializer(
                            entityDefId = EntityDefId(rc.entityId),
                            name = rc.name?.let { LocalizedTextNotLocalized(it) },
                            description = rc.description?.let { LocalizedTextNotLocalized(it) },
                            documentationHome = rc.documentationHome?.let { URI(it).toURL() },
                            identityAttribute = AttributeDefIdentityInitializer(
                                attributeDefId = AttributeDefId(rc.identityAttributeId),
                                type = rc.identityAttributeType,
                                name = rc.name?.let { LocalizedTextNotLocalized(it) },
                                description = rc.description?.let { LocalizedTextNotLocalized(it) },
                            )
                        ),
                    )
                )
            }

            is ModelAction.UpdateEntityId -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.UpdateEntityDef(
                        modelId = ModelId(rc.modelId),
                        entityDefId = EntityDefId(rc.entityId),
                        cmd = EntityDefUpdateCmd.Id(EntityDefId(rc.value))
                    )
                )
            }

            is ModelAction.UpdateEntityName -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.UpdateEntityDef(
                        modelId = ModelId(rc.modelId),
                        entityDefId = EntityDefId(rc.entityId),
                        cmd = EntityDefUpdateCmd.Name(rc.value?.let { LocalizedTextNotLocalized(it) })
                    )
                )
            }

            is ModelAction.UpdateEntityDescription -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.UpdateEntityDef(
                        modelId = ModelId(rc.modelId),
                        entityDefId = EntityDefId(rc.entityId),
                        cmd = EntityDefUpdateCmd.Description(rc.value?.let { LocalizedTextNotLocalized(it) })
                    )
                )
            }

            is ModelAction.DeleteEntity -> {
                actionCtx.modelCmds.dispatch(
                    ModelCmd.DeleteEntityDef(
                        modelId = ModelId(rc.modelId),
                        entityDefId = EntityDefId(rc.entityId)
                    )
                )
            }

            // ------------------------------------------------------------------------
            // Entity attributes
            // ------------------------------------------------------------------------

            is ModelAction.CreateEntityAttribute -> {
                dispatch(
                    ModelCmd.CreateEntityDefAttributeDef(
                        modelId = ModelId(rc.modelId),
                        entityDefId = EntityDefId(rc.entityId),
                        attributeDefInitializer = AttributeDefInitializer(
                            attributeDefId = AttributeDefId(rc.attributeId),
                            type = ModelTypeId(rc.type),
                            optional = rc.optional,
                            name = rc.name?.let { LocalizedTextNotLocalized(it) },
                            description = rc.description?.let { LocalizedTextNotLocalized(it) },
                        )
                    )
                )
            }

            is ModelAction.UpdateEntityAttributeId -> {
                dispatch(
                    ModelCmd.UpdateEntityDefAttributeDef(
                        modelId = ModelId(rc.modelId),
                        entityDefId = EntityDefId(rc.entityId),
                        attributeDefId = AttributeDefId(rc.attributeId),
                        cmd = AttributeDefUpdateCmd.Id(AttributeDefId(rc.value))
                    )
                )
            }

            is ModelAction.UpdateEntityAttributeName -> {
                dispatch(
                    ModelCmd.UpdateEntityDefAttributeDef(
                        modelId = ModelId(rc.modelId),
                        entityDefId = EntityDefId(rc.entityId),
                        attributeDefId = AttributeDefId(rc.attributeId),
                        cmd = AttributeDefUpdateCmd.Name(rc.value?.let { LocalizedTextNotLocalized(it) })
                    )
                )
            }

            is ModelAction.UpdateEntityAttributeDescription -> {
                dispatch(
                    ModelCmd.UpdateEntityDefAttributeDef(
                        modelId = ModelId(rc.modelId),
                        entityDefId = EntityDefId(rc.entityId),
                        attributeDefId = AttributeDefId(rc.attributeId),
                        cmd = AttributeDefUpdateCmd.Description(rc.value?.let { LocalizedTextNotLocalized(it) })
                    )
                )
            }

            is ModelAction.UpdateEntityAttributeType -> {
                dispatch(
                    ModelCmd.UpdateEntityDefAttributeDef(
                        modelId = ModelId(rc.modelId),
                        entityDefId = EntityDefId(rc.entityId),
                        attributeDefId = AttributeDefId(rc.attributeId),
                        cmd = AttributeDefUpdateCmd.Type(ModelTypeId(rc.value))
                    )
                )
            }

            is ModelAction.UpdateEntityAttributeOptional -> {
                dispatch(
                    ModelCmd.UpdateEntityDefAttributeDef(
                        modelId = ModelId(rc.modelId),
                        entityDefId = EntityDefId(rc.entityId),
                        attributeDefId = AttributeDefId(rc.attributeId),
                        cmd = AttributeDefUpdateCmd.Optional(rc.value)
                    )
                )
            }

            is ModelAction.DeleteEntityAttribute -> {
                dispatch(
                    ModelCmd.DeleteEntityDefAttributeDef(
                        modelId = ModelId(rc.modelId),
                        entityDefId = EntityDefId(rc.entityId),
                        attributeDefId = AttributeDefId(rc.attributeId)
                    )
                )
            }


            // ------------------------------------------------------------------------
            // Relationships
            // ------------------------------------------------------------------------

            is ModelAction.CreateRelationshipDef -> dispatch(
                ModelCmd.CreateRelationshipDef(
                    modelId = rc.modelId,
                    initializer = rc.initializer
                )
            )

            is ModelAction.UpdateRelationshipDef -> dispatch(
                ModelCmd.UpdateRelationshipDef(
                    modelId = rc.modelId,
                    relationshipDefId = rc.relationshipDefId,
                    cmd = rc.cmd
                )
            )

            is ModelAction.DeleteRelationshipDef -> dispatch(
                ModelCmd.DeleteRelationshipDef(
                    modelId = rc.modelId,
                    relationshipDefId = rc.relationshipDefId
                )
            )

            is ModelAction.CreateRelationshipAttributeDef -> dispatch(
                ModelCmd.CreateRelationshipAttributeDef(
                    modelId = rc.modelId,
                    relationshipDefId = rc.relationshipDefId,
                    attr = rc.attr
                )
            )

            is ModelAction.UpdateRelationshipAttributeDef -> dispatch(
                ModelCmd.UpdateRelationshipAttributeDef(
                    modelId = rc.modelId,
                    relationshipDefId = rc.relationshipDefId,
                    attributeDefId = rc.attributeDefId,
                    cmd = rc.cmd
                )
            )

            is ModelAction.DeleteRelationshipAttributeDef -> dispatch(
                ModelCmd.DeleteRelationshipAttributeDef(
                    modelId = rc.modelId,
                    relationshipDefId = rc.relationshipDefId,
                    attributeDefId = rc.attributeDefId,
                )
            )
        }
        return result
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ModelActionProvider::class.java)
    }
}
