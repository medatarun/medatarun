package io.medatarun.resources

import io.medatarun.model.model.*
import io.medatarun.resources.actions.ModelImportAction
import io.medatarun.resources.actions.ModelInspectAction
import io.medatarun.resources.actions.ModelInspectJsonAction
import io.medatarun.runtime.AppRuntime
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.file.FileSystems

class ModelResource(private val runtime: AppRuntime) : ResourceContainer<ModelResourceCmd> {

    /**
     * Returns the list of supported commands. Note that we NEVER return the business model's commands
     * but something mode user-facing so that the model can evolve with preserving maximum compatibility
     * with user facing actions.
     */
    override fun findCommandClass() = ModelResourceCmd::class

    override fun dispatch(cmd: ModelResourceCmd): Any? {
        val rc = cmd as ModelResourceCmd

        fun dispatch(businessCmd: ModelCmd) = runtime.modelCmds.dispatch(businessCmd)

        logger.info(rc.toString())

        val result = when (rc) {

            // ------------------------------------------------------------------------
            // Models
            // ------------------------------------------------------------------------

            is ModelResourceCmd.Import -> ModelImportAction(runtime, FileSystems.getDefault()).process(rc)
            is ModelResourceCmd.Inspect -> ModelInspectAction(runtime).process()
            is ModelResourceCmd.InspectJson -> ModelInspectJsonAction(runtime).process()
            is ModelResourceCmd.CreateModel -> {
                runtime.modelCmds.dispatch(ModelCmd.CreateModel(
                    id = ModelId(rc.id),
                    name = LocalizedTextNotLocalized(rc.name),
                    description = rc.description?.let { LocalizedTextNotLocalized(it) },
                    version = rc.version ?: ModelVersion("1.0.0")
                ))
            }

            is ModelResourceCmd.UpdateModelName -> {
                runtime.modelCmds.dispatch(ModelCmd.UpdateModelName(
                    modelId = ModelId(rc.id),
                    name = LocalizedTextNotLocalized(rc.name),
                ))
            }

            is ModelResourceCmd.UpdateModelDescription -> {
                runtime.modelCmds.dispatch(ModelCmd.UpdateModelDescription(
                    modelId = ModelId(rc.id),
                    description = rc.description?.let { LocalizedTextNotLocalized(it) },
                ))
            }

            is ModelResourceCmd.UpdateModelVersion -> {
                runtime.modelCmds.dispatch(ModelCmd.UpdateModelVersion(
                    modelId = ModelId(rc.id),
                    version = ModelVersion(rc.version),
                ))
            }

            is ModelResourceCmd.DeleteModel -> {
                runtime.modelCmds.dispatch(ModelCmd.DeleteModel(ModelId(rc.id)))
            }

            // ------------------------------------------------------------------------
            // Types
            // ------------------------------------------------------------------------

            is ModelResourceCmd.CreateType -> {
                runtime.modelCmds.dispatch(ModelCmd.CreateType(
                    modelId = ModelId(rc.modelId),
                    initializer = ModelTypeInitializer(
                        id = ModelTypeId(rc.typeId),
                        name = rc.name?.let { LocalizedTextNotLocalized(it) },
                        description = rc.description?.let { LocalizedTextNotLocalized(it) },
                    )
                ))
            }

            is ModelResourceCmd.UpdateTypeName -> {
                runtime.modelCmds.dispatch(ModelCmd.UpdateType(
                    modelId = ModelId(rc.modelId),
                    typeId = ModelTypeId(rc.typeId),
                    cmd = ModelTypeUpdateCmd.Name(rc.name?.let { LocalizedTextNotLocalized(it) })
                ))
            }

            is ModelResourceCmd.UpdateTypeDescription -> {
                runtime.modelCmds.dispatch(ModelCmd.UpdateType(
                    modelId = ModelId(rc.modelId),
                    typeId = ModelTypeId(rc.typeId),
                    cmd = ModelTypeUpdateCmd.Description(rc.description?.let { LocalizedTextNotLocalized(it) })
                ))
            }

            is ModelResourceCmd.DeleteType -> {
                runtime.modelCmds.dispatch(ModelCmd.DeleteType(
                    modelId = ModelId(rc.modelId),
                    typeId = ModelTypeId(rc.typeId),
                ))
            }

            // ------------------------------------------------------------------------
            // Entities
            // ------------------------------------------------------------------------

            is ModelResourceCmd.CreateEntity -> {
                runtime.modelCmds.dispatch(ModelCmd.CreateEntityDef(
                    modelId = ModelId(rc.modelId),
                    entityDefInitializer = EntityDefInitializer(
                        entityDefId = EntityDefId(rc.entityId),
                        name = rc.name?.let { LocalizedTextNotLocalized(it) },
                        description = rc.description?.let { LocalizedTextNotLocalized(it) },
                        documentationHome = rc.documentationHome?.let { URI(it).toURL()},
                        identityAttribute = AttributeDefIdentityInitializer(
                            attributeDefId = AttributeDefId(rc.identityAttributeId),
                            type = rc.identityAttributeType,
                            name = rc.name?.let { LocalizedTextNotLocalized(it) },
                            description = rc.description?.let { LocalizedTextNotLocalized(it) },
                        )
                    ),
                ))
            }

            is ModelResourceCmd.UpdateEntityId -> {
                runtime.modelCmds.dispatch(ModelCmd.UpdateEntityDef(
                    modelId = ModelId(rc.modelId),
                    entityDefId = EntityDefId(rc.entityId),
                    cmd = EntityDefUpdateCmd.Id(EntityDefId(rc.value))
                ))
            }

            is ModelResourceCmd.UpdateEntityName -> {
                runtime.modelCmds.dispatch(ModelCmd.UpdateEntityDef(
                    modelId = ModelId(rc.modelId),
                    entityDefId = EntityDefId(rc.entityId),
                    cmd = EntityDefUpdateCmd.Name(rc.value?.let { LocalizedTextNotLocalized(it) })
                ))
            }

            is ModelResourceCmd.UpdateEntityDescription -> {
                runtime.modelCmds.dispatch(ModelCmd.UpdateEntityDef(
                    modelId = ModelId(rc.modelId),
                    entityDefId = EntityDefId(rc.entityId),
                    cmd = EntityDefUpdateCmd.Description(rc.value?.let { LocalizedTextNotLocalized(it) })
                ))
            }

            is ModelResourceCmd.DeleteEntity -> {
                runtime.modelCmds.dispatch(ModelCmd.DeleteEntityDef(
                    modelId = ModelId(rc.modelId),
                    entityDefId = EntityDefId(rc.entityId)
                ))
            }

            // ------------------------------------------------------------------------
            // Entity attributes
            // ------------------------------------------------------------------------

            is ModelResourceCmd.CreateEntityAttribute -> {
                dispatch(ModelCmd.CreateEntityDefAttributeDef(
                    modelId = ModelId(rc.modelId),
                    entityDefId = EntityDefId(rc.entityId),
                    attributeDefInitializer = AttributeDefInitializer(
                        attributeDefId = AttributeDefId(rc.attributeId),
                        type = ModelTypeId(rc.type),
                        optional = rc.optional,
                        name = rc.name?.let { LocalizedTextNotLocalized(it) },
                        description = rc.description?.let { LocalizedTextNotLocalized(it) },
                    )
                ))
            }

            is ModelResourceCmd.UpdateEntityAttributeId -> {
                dispatch(ModelCmd.UpdateEntityDefAttributeDef(
                    modelId = ModelId(rc.modelId),
                    entityDefId = EntityDefId(rc.entityId),
                    attributeDefId = AttributeDefId(rc.attributeId),
                    cmd = AttributeDefUpdateCmd.Id(AttributeDefId(rc.value))
                ))
            }

            is ModelResourceCmd.UpdateEntityAttributeName -> {
                dispatch(ModelCmd.UpdateEntityDefAttributeDef(
                    modelId = ModelId(rc.modelId),
                    entityDefId = EntityDefId(rc.entityId),
                    attributeDefId = AttributeDefId(rc.attributeId),
                    cmd = AttributeDefUpdateCmd.Name(rc.value?.let { LocalizedTextNotLocalized(it) })
                ))
            }

            is ModelResourceCmd.UpdateEntityAttributeDescription -> {
                dispatch(ModelCmd.UpdateEntityDefAttributeDef(
                    modelId = ModelId(rc.modelId),
                    entityDefId = EntityDefId(rc.entityId),
                    attributeDefId = AttributeDefId(rc.attributeId),
                    cmd = AttributeDefUpdateCmd.Description(rc.value?.let { LocalizedTextNotLocalized(it) })
                ))
            }

            is ModelResourceCmd.UpdateEntityAttributeType -> {
                dispatch(ModelCmd.UpdateEntityDefAttributeDef(
                    modelId = ModelId(rc.modelId),
                    entityDefId = EntityDefId(rc.entityId),
                    attributeDefId = AttributeDefId(rc.attributeId),
                    cmd = AttributeDefUpdateCmd.Type(ModelTypeId(rc.value))
                ))
            }

            is ModelResourceCmd.UpdateEntityAttributeOptional -> {
                dispatch(ModelCmd.UpdateEntityDefAttributeDef(
                    modelId = ModelId(rc.modelId),
                    entityDefId = EntityDefId(rc.entityId),
                    attributeDefId = AttributeDefId(rc.attributeId),
                    cmd = AttributeDefUpdateCmd.Optional(rc.value)
                ))
            }

            is ModelResourceCmd.DeleteEntityAttribute -> {
                dispatch(ModelCmd.DeleteEntityDefAttributeDef(
                    modelId = ModelId(rc.modelId),
                    entityDefId = EntityDefId(rc.entityId),
                    attributeDefId = AttributeDefId(rc.attributeId)
                ))
            }


            // ------------------------------------------------------------------------
            // Relationships
            // ------------------------------------------------------------------------

            is ModelResourceCmd.CreateRelationshipDef -> dispatch(
                ModelCmd.CreateRelationshipDef(
                    modelId = rc.modelId,
                    initializer = rc.initializer
                )
            )

            is ModelResourceCmd.UpdateRelationshipDef -> dispatch(
                ModelCmd.UpdateRelationshipDef(
                    modelId = rc.modelId,
                    relationshipDefId = rc.relationshipDefId,
                    cmd = rc.cmd
                )
            )

            is ModelResourceCmd.DeleteRelationshipDef -> dispatch(
                ModelCmd.DeleteRelationshipDef(
                    modelId = rc.modelId,
                    relationshipDefId = rc.relationshipDefId
                )
            )

            is ModelResourceCmd.CreateRelationshipAttributeDef -> dispatch(
                ModelCmd.CreateRelationshipAttributeDef(
                    modelId = rc.modelId,
                    relationshipDefId = rc.relationshipDefId,
                    attr = rc.attr
                )
            )

            is ModelResourceCmd.UpdateRelationshipAttributeDef -> dispatch(
                ModelCmd.UpdateRelationshipAttributeDef(
                    modelId = rc.modelId,
                    relationshipDefId = rc.relationshipDefId,
                    attributeDefId = rc.attributeDefId,
                    cmd = rc.cmd
                )
            )

            is ModelResourceCmd.DeleteRelationshipAttributeDef -> dispatch(
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
        private val logger = LoggerFactory.getLogger(ModelResource::class.java)
    }
}
