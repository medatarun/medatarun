package io.medatarun.model.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.getService
import io.medatarun.kernel.ResourceLocator
import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.*
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

    override fun dispatch(cmd: ModelAction, actionCtx: ActionCtx): Any? {
        val rc = cmd as ModelAction

        val modelCmds = actionCtx.getService<ModelCmds>()
        val modelQueries = actionCtx.getService<ModelQueries>()
        val modelHumanPrinter = actionCtx.getService<ModelHumanPrinter>()

        fun dispatch(businessCmd: ModelCmd) = modelCmds.dispatch(businessCmd)

        logger.info(rc.toString())

        val result = when (rc) {

            // ------------------------------------------------------------------------
            // Models
            // ------------------------------------------------------------------------

            is ModelAction.Import -> ModelImportAction(actionCtx.extensionRegistry, modelCmds,  resourceLocator).process(rc)
            is ModelAction.Inspect_Human -> ModelInspectAction(modelQueries, modelHumanPrinter).process()
            is ModelAction.Inspect_Json -> ModelInspectJsonAction(modelQueries).process()
            is ModelAction.Model_Create -> {
                modelCmds.dispatch(
                    ModelCmd.CreateModel(
                        modelKey = rc.modelKey.validated(),
                        name = LocalizedTextNotLocalized(rc.name),
                        description = rc.description?.let { LocalizedTextNotLocalized(it) },
                        version = rc.version ?: ModelVersion("0.0.1")
                    )
                )
            }
            is ModelAction.Model_Copy -> {
                modelCmds.dispatch(
                    ModelCmd.CopyModel(
                        modelKey = rc.modelKey.validated(),
                        modelNewKey = rc.modelNewKey.validated()
                    )
                )
            }

            is ModelAction.Model_UpdateName -> {
                modelCmds.dispatch(
                    ModelCmd.UpdateModelName(
                        modelKey = rc.modelKey.validated(),
                        name = LocalizedTextNotLocalized(rc.name),
                    )
                )
            }

            is ModelAction.Model_UpdateDescription -> {
                modelCmds.dispatch(
                    ModelCmd.UpdateModelDescription(
                        modelKey = rc.modelKey.validated(),
                        description = rc.description?.let { LocalizedTextNotLocalized(it) },
                    )
                )
            }

            is ModelAction.Model_UpdateVersion -> {
                modelCmds.dispatch(
                    ModelCmd.UpdateModelVersion(
                        modelKey = rc.modelKey.validated(),
                        version = rc.version,
                    )
                )
            }

            is ModelAction.Model_AddTag -> modelCmds.dispatch(
                ModelCmd.UpdateModelHashtagAdd(
                    modelKey = rc.modelKey.validated(),
                    hashtag = rc.tag.validated()
                )
            )

            is ModelAction.Model_DeleteTag -> modelCmds.dispatch(
                ModelCmd.UpdateModelHashtagDelete(
                    modelKey = rc.modelKey.validated(),
                    hashtag = rc.tag.validated()
                )
            )

            is ModelAction.Model_Delete -> {
                modelCmds.dispatch(ModelCmd.DeleteModel(rc.modelKey.validated()))
            }

            // ------------------------------------------------------------------------
            // Types
            // ------------------------------------------------------------------------

            is ModelAction.Type_Create -> {
                modelCmds.dispatch(
                    ModelCmd.CreateType(
                        modelKey = rc.modelKey.validated(),
                        initializer = ModelTypeInitializer(
                            id = rc.typeKey.validated(),
                            name = rc.name?.let { LocalizedTextNotLocalized(it) },
                            description = rc.description?.let { LocalizedTextNotLocalized(it) },
                        )
                    )
                )
            }

            is ModelAction.Type_UpdateName -> {
                modelCmds.dispatch(
                    ModelCmd.UpdateType(
                        modelKey = rc.modelKey.validated(),
                        typeId = rc.typeKey.validated(),
                        cmd = ModelTypeUpdateCmd.Name(rc.name?.let { LocalizedTextNotLocalized(it) })
                    )
                )
            }

            is ModelAction.Type_UpdateDescription -> {
                modelCmds.dispatch(
                    ModelCmd.UpdateType(
                        modelKey = rc.modelKey.validated(),
                        typeId = rc.typeKey.validated(),
                        cmd = ModelTypeUpdateCmd.Description(rc.description?.let { LocalizedTextNotLocalized(it) })
                    )
                )
            }

            is ModelAction.Type_Delete -> {
                modelCmds.dispatch(
                    ModelCmd.DeleteType(
                        modelKey = rc.modelKey.validated(),
                        typeId = rc.typeKey.validated(),
                    )
                )
            }

            // ------------------------------------------------------------------------
            // Entities
            // ------------------------------------------------------------------------

            is ModelAction.Entity_Create -> {
                modelCmds.dispatch(
                    ModelCmd.CreateEntityDef(
                        modelKey = rc.modelKey.validated(),
                        entityDefInitializer = EntityDefInitializer(
                            entityKey = rc.entityKey.validated(),
                            name = rc.name?.let { LocalizedTextNotLocalized(it) },
                            description = rc.description?.let { LocalizedTextNotLocalized(it) },
                            documentationHome = rc.documentationHome?.let { URI(it).toURL() },
                            identityAttribute = AttributeDefIdentityInitializer(
                                attributeKey = rc.identityAttributeKey,
                                type = rc.identityAttributeType,
                                name = rc.name?.let { LocalizedTextNotLocalized(it) },
                                description = rc.description?.let { LocalizedTextNotLocalized(it) },
                            )
                        ),
                    )
                )
            }

            is ModelAction.Entity_UpdateId -> {
                modelCmds.dispatch(
                    ModelCmd.UpdateEntityDef(
                        modelKey = rc.modelKey.validated(),
                        entityKey = rc.entityKey.validated(),
                        cmd = EntityDefUpdateCmd.Id(EntityKey(rc.value))
                    )
                )
            }

            is ModelAction.Entity_UpdateName -> {
                modelCmds.dispatch(
                    ModelCmd.UpdateEntityDef(
                        modelKey = rc.modelKey.validated(),
                        entityKey = rc.entityKey.validated(),
                        cmd = EntityDefUpdateCmd.Name(rc.value?.let { LocalizedTextNotLocalized(it) })
                    )
                )
            }

            is ModelAction.Entity_UpdateDescription -> {
                modelCmds.dispatch(
                    ModelCmd.UpdateEntityDef(
                        modelKey = rc.modelKey.validated(),
                        entityKey = rc.entityKey.validated(),
                        cmd = EntityDefUpdateCmd.Description(rc.value?.let { LocalizedTextNotLocalized(it) })
                    )
                )
            }

            is ModelAction.Entity_AddTag -> modelCmds.dispatch(
                ModelCmd.UpdateEntityDefHashtagAdd(
                    modelKey = rc.modelKey.validated(),
                    entityKey = rc.entityKey.validated(),
                    hashtag = rc.tag.validated()
                )
            )

            is ModelAction.Entity_DeleteTag -> modelCmds.dispatch(
                ModelCmd.UpdateEntityDefHashtagDelete(
                    modelKey = rc.modelKey.validated(),
                    entityKey = rc.entityKey.validated(),
                    hashtag = rc.tag.validated()
                )
            )

            is ModelAction.Entity_Delete -> {
                modelCmds.dispatch(
                    ModelCmd.DeleteEntityDef(
                        modelKey = rc.modelKey.validated(),
                        entityKey = rc.entityKey.validated()
                    )
                )
            }

            // ------------------------------------------------------------------------
            // Entity attributes
            // ------------------------------------------------------------------------

            is ModelAction.EntityAttribute_Create -> {
                dispatch(
                    ModelCmd.CreateEntityDefAttributeDef(
                        modelKey = rc.modelKey.validated(),
                        entityKey = rc.entityKey.validated(),
                        attributeDefInitializer = AttributeDefInitializer(
                            attributeKey = rc.attributeKey,
                            type = rc.type.validated(),
                            optional = rc.optional,
                            name = rc.name?.let { LocalizedTextNotLocalized(it) },
                            description = rc.description?.let { LocalizedTextNotLocalized(it) },
                        )
                    )
                )
            }

            is ModelAction.EntityAttribute_UpdateId -> {
                dispatch(
                    ModelCmd.UpdateEntityDefAttributeDef(
                        modelKey = rc.modelKey.validated(),
                        entityKey = rc.entityKey.validated(),
                        attributeKey = rc.attributeKey,
                        cmd = AttributeDefUpdateCmd.Id(AttributeKey(rc.value))
                    )
                )
            }

            is ModelAction.EntityAttribute_UpdateName -> {
                dispatch(
                    ModelCmd.UpdateEntityDefAttributeDef(
                        modelKey = rc.modelKey.validated(),
                        entityKey = rc.entityKey.validated(),
                        attributeKey = rc.attributeKey,
                        cmd = AttributeDefUpdateCmd.Name(rc.value?.let { LocalizedTextNotLocalized(it) })
                    )
                )
            }

            is ModelAction.EntityAttribute_UpdateDescription -> {
                dispatch(
                    ModelCmd.UpdateEntityDefAttributeDef(
                        modelKey = rc.modelKey.validated(),
                        entityKey = rc.entityKey.validated(),
                        attributeKey = rc.attributeKey,
                        cmd = AttributeDefUpdateCmd.Description(rc.value?.let { LocalizedTextNotLocalized(it) })
                    )
                )
            }

            is ModelAction.EntityAttribute_UpdateType -> {
                dispatch(
                    ModelCmd.UpdateEntityDefAttributeDef(
                        modelKey = rc.modelKey.validated(),
                        entityKey = rc.entityKey.validated(),
                        attributeKey = rc.attributeKey,
                        cmd = AttributeDefUpdateCmd.Type(TypeKey(rc.value))
                    )
                )
            }

            is ModelAction.EntityAttribute_UpdateOptional -> {
                dispatch(
                    ModelCmd.UpdateEntityDefAttributeDef(
                        modelKey = rc.modelKey.validated(),
                        entityKey = rc.entityKey.validated(),
                        attributeKey = rc.attributeKey,
                        cmd = AttributeDefUpdateCmd.Optional(rc.value)
                    )
                )
            }

            is ModelAction.EntityAttribute_AddTag -> modelCmds.dispatch(
                ModelCmd.UpdateEntityDefAttributeDefHashtagAdd(
                    modelKey = rc.modelKey.validated(),
                    entityKey = rc.entityKey.validated(),
                    attributeKey = rc.attributeKey,
                    hashtag = rc.tag.validated()
                )
            )

            is ModelAction.EntityAttribute_DeleteTag -> modelCmds.dispatch(
                ModelCmd.UpdateEntityDefAttributeDefHashtagDelete(
                    modelKey = rc.modelKey.validated(),
                    entityKey = rc.entityKey.validated(),
                    attributeKey = rc.attributeKey,
                    hashtag = rc.tag.validated()
                )
            )

            is ModelAction.EntityAttribute_Delete -> {
                dispatch(
                    ModelCmd.DeleteEntityDefAttributeDef(
                        modelKey = rc.modelKey.validated(),
                        entityKey = rc.entityKey.validated(),
                        attributeKey = rc.attributeKey
                    )
                )
            }


            // ------------------------------------------------------------------------
            // Relationships
            // ------------------------------------------------------------------------

            is ModelAction.Relationship_Create -> dispatch(
                ModelCmd.CreateRelationshipDef(
                    modelKey = rc.modelKey.validated(),
                    initializer = rc.initializer
                )
            )

            is ModelAction.Relationship_Update -> dispatch(
                ModelCmd.UpdateRelationshipDef(
                    modelKey = rc.modelKey.validated(),
                    relationshipKey = rc.relationshipKey.validated(),
                    cmd = rc.cmd
                )
            )

            is ModelAction.Relationship_AddTag -> modelCmds.dispatch(
                ModelCmd.UpdateRelationshipDefHashtagAdd(
                    modelKey = rc.modelKey.validated(),
                    relationshipKey = rc.relationshipKey.validated(),
                    hashtag = rc.tag.validated()
                )
            )

            is ModelAction.Relationship_DeleteTag -> modelCmds.dispatch(
                ModelCmd.UpdateRelationshipDefHashtagDelete(
                    modelKey = rc.modelKey.validated(),
                    relationshipKey = rc.relationshipKey.validated(),
                    hashtag = rc.tag.validated()
                )
            )

            is ModelAction.Relationship_Delete -> dispatch(
                ModelCmd.DeleteRelationshipDef(
                    modelKey = rc.modelKey.validated(),
                    relationshipKey = rc.relationshipKey.validated()
                )
            )

            is ModelAction.RelationshipAttribute_Create -> dispatch(
                ModelCmd.CreateRelationshipAttributeDef(
                    modelKey = rc.modelKey.validated(),
                    relationshipKey = rc.relationshipKey.validated(),
                    attr = rc.attr
                )
            )

            is ModelAction.RelationshipAttribute_Update -> dispatch(
                ModelCmd.UpdateRelationshipAttributeDef(
                    modelKey = rc.modelKey.validated(),
                    relationshipKey = rc.relationshipKey.validated(),
                    attributeKey = rc.attributeKey,
                    cmd = rc.cmd
                )
            )

            is ModelAction.RelationshipAttribute_AddTag -> dispatch(
                ModelCmd.UpdateRelationshipAttributeDefHashtagAdd(
                    modelKey = rc.modelKey.validated(),
                    relationshipKey = rc.relationshipKey.validated(),
                    attributeKey = rc.attributeKey,
                    hashtag = rc.tag.validated()
                )
            )

            is ModelAction.RelationshipAttribute_DeleteTag -> dispatch(
                ModelCmd.UpdateRelationshipAttributeDefHashtagDelete(
                    modelKey = rc.modelKey.validated(),
                    relationshipKey = rc.relationshipKey.validated(),
                    attributeKey = rc.attributeKey,
                    hashtag = rc.tag.validated()
                )
            )

            is ModelAction.RelationshipAttribute_Delete -> dispatch(
                ModelCmd.DeleteRelationshipAttributeDef(
                    modelKey = rc.modelKey.validated(),
                    relationshipKey = rc.relationshipKey.validated(),
                    attributeKey = rc.attributeKey,
                )
            )
        }
        return result
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ModelActionProvider::class.java)
    }
}
