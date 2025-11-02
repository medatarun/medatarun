package io.medatarun.model.internal

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelRepositoryCmd
import io.medatarun.model.ports.ModelStorages
import io.medatarun.model.ports.RepositoryRef

class ModelCmdsImpl(
    val storage: ModelStorages,
    val auditor: ModelAuditor
) : ModelCmds {

    override fun createModel(
        id: ModelId,
        name: LocalizedText,
        description: LocalizedMarkdown?,
        version: ModelVersion,
        repositoryRef: RepositoryRef
    ) {
        val model = ModelInMemory(
            id = id,
            name = name,
            description = description,
            version = version,
            types = emptyList(),
            entityDefs = emptyList(),
            relationshipDefs = emptyList(),
        )
        storage.createModel(model, repositoryRef)
    }

    override fun importModel(model: Model, repositoryRef: RepositoryRef) {
        storage.createModel(model, repositoryRef)
    }

    override fun deleteModel(modelId: ModelId) {
        ensureModelExists(modelId)
        storage.deleteModel(modelId)
    }

    override fun updateModelName(modelId: ModelId, name: LocalizedTextNotLocalized) {
        ensureModelExists(modelId)
        storage.updateModelName(modelId, name)
    }

    override fun updateModelDescription(modelId: ModelId, description: LocalizedTextNotLocalized?) {
        ensureModelExists(modelId)
        storage.updateModelDescription(modelId, description)
    }

    override fun updateModelVersion(modelId: ModelId, version: ModelVersion) {
        ensureModelExists(modelId)
        storage.updateModelVersion(modelId, version)
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Types
    // -----------------------------------------------------------------------------------------------------------------

    private fun createType(c: ModelCmd.CreateType) {
        // Can not create a type if another has the same type
        val model = storage.findModelByIdOptional(c.modelId) ?: throw ModelNotFoundException(c.modelId)
        val existing = model.findTypeOptional(c.initializer.id)
        if (existing != null) throw TypeCreateDuplicateException(c.modelId, c.initializer.id)
        storage.dispatch(ModelRepositoryCmd.CreateType(c.modelId, c.initializer))
    }

    private fun updateType(c: ModelCmd.UpdateType) {
        val model = storage.findModelByIdOptional(c.modelId) ?: throw ModelNotFoundException(c.modelId)
        model.findTypeOptional(c.typeId) ?: throw TypeNotFoundException(c.modelId, c.typeId)
        storage.dispatch(ModelRepositoryCmd.UpdateType(c.modelId, c.typeId, c.cmd))
    }

    private fun deleteType(c: ModelCmd.DeleteType) {
        // Can not delete type used in any entity
        val model = storage.findModelByIdOptional(c.modelId) ?: throw ModelNotFoundException(c.modelId)
        val used = model.entityDefs.any { entityDef -> entityDef.attributes.any { attr -> attr.type == c.typeId } }
        if (used) throw ModelTypeDeleteUsedException(c.typeId)
        model.findTypeOptional(c.typeId) ?: throw TypeNotFoundException(c.modelId, c.typeId)
        storage.dispatch(ModelRepositoryCmd.DeleteType(c.modelId, c.typeId))
    }
    // -----------------------------------------------------------------------------------------------------------------
    // Entities
    // -----------------------------------------------------------------------------------------------------------------

    private fun updateEntityDef(c: ModelCmd.UpdateEntityDef) {
        ensureModelExists(c.modelId)
        val model = storage.findModelById(c.modelId)
        model.findEntityDef(c.entityDefId)
        if (c.cmd is EntityDefUpdateCmd.Id) {
            if (model.entityDefs.any { it.id == c.cmd.value && it.id != c.entityDefId }) {
                throw UpdateEntityDefIdDuplicateIdException(c.entityDefId)
            }
        }
        storage.dispatch(ModelRepositoryCmd.UpdateEntityDef(c.modelId, c.entityDefId, c.cmd))
    }


    private fun createEntityDef(c: ModelCmd.CreateEntityDef) {

        findModelById(c.modelId).ensureTypeExists(c.entityDefInitializer.identityAttribute.type)
        storage.dispatch(ModelRepositoryCmd.CreateEntityDef(
            c.modelId, EntityDefInMemory(
                id = c.entityDefInitializer.entityDefId,
                name = c.entityDefInitializer.name,
                description = c.entityDefInitializer.description,
                identifierAttributeDefId = c.entityDefInitializer.identityAttribute.attributeDefId,
                attributes = listOf(
                    AttributeDefInMemory(
                        id = c.entityDefInitializer.identityAttribute.attributeDefId,
                        name = c.entityDefInitializer.identityAttribute.name,
                        description = c.entityDefInitializer.identityAttribute.description,
                        type = c.entityDefInitializer.identityAttribute.type,
                        optional = false // because it's identity, can never be optional
                    )
                )
            )
        ))
    }

    private fun deleteEntityDef(c: ModelCmd.DeleteEntityDef) {
        storage.dispatch(ModelRepositoryCmd.DeleteEntityDef(modelId= c.modelId, entityDefId=c.entityDefId))
    }

    private fun createEntityDefAttributeDef(c: ModelCmd.CreateEntityDefAttributeDef) {
        val e = findModelById(c.modelId).findEntityDef(c.entityDefId)
        if (e.hasAttributeDef(c.attributeDefInitializer.attributeDefId)) throw CreateAttributeDefDuplicateIdException(
            c.entityDefId,
            c.attributeDefInitializer.attributeDefId
        )
        findModelById(c.modelId).ensureTypeExists(c.attributeDefInitializer.type)
        storage.dispatch(
            ModelRepositoryCmd.CreateEntityDefAttributeDef(
                modelId = c.modelId,
                entityDefId = c.entityDefId,
                attributeDef = AttributeDefInMemory(
                    id = c.attributeDefInitializer.attributeDefId,
                    name = c.attributeDefInitializer.name,
                    description = c.attributeDefInitializer.description,
                    type = c.attributeDefInitializer.type,
                    optional = c.attributeDefInitializer.optional
                )
            )
        )
    }

    private fun deleteEntityDefAttributeDef(c: ModelCmd.DeleteEntityDefAttributeDef) {
        val entity = findModelById(c.modelId).findEntityDef(c.entityDefId)
        if (entity.identifierAttributeDefId == c.attributeDefId)
            throw DeleteAttributeIdentifierException(c.modelId, c.entityDefId, c.attributeDefId)
        storage.dispatch(
            ModelRepositoryCmd.DeleteEntityDefAttributeDef(
                modelId = c.modelId,
                entityDefId = c.entityDefId,
                attributeDefId = c.attributeDefId
            )
        )
    }

    private fun updateEntityDefAttributeDef(c: ModelCmd.UpdateEntityDefAttributeDef) {
        val model = findModelById(c.modelId)
        val entity = model.findEntityDef(c.entityDefId)
        entity.ensureAttributeDefExists(c.attributeDefId)

        // TODO how do we ensure transactions here ?


        if (c.cmd is AttributeDefUpdateCmd.Id) {
            // We can not have two attributes with the same id
            if (entity.attributes.any { it.id == c.cmd.value && it.id != c.attributeDefId }) {
                throw UpdateAttributeDefDuplicateIdException(c.entityDefId, c.attributeDefId)
            }
            // If user wants to rename the Entity's identity attribute, we must rename in entity
            // as well as the attribute's id, then apply changes on entity
            if (entity.identifierAttributeDefId == c.attributeDefId) {
                storage.dispatch(ModelRepositoryCmd.UpdateEntityDef(
                    modelId = c.modelId,
                    entityDefId = c.entityDefId,
                    cmd = EntityDefUpdateCmd.IdentifierAttribute(c.cmd.value)
                ))
            }
        } else if (c.cmd is AttributeDefUpdateCmd.Type) {
            // Attribute type shall exist when updating types
            findModelById(c.modelId).ensureTypeExists(c.cmd.value)
        }

        // Apply changes on attribute
        storage.dispatch(
            ModelRepositoryCmd.UpdateEntityDefAttributeDef(
                modelId = c.modelId,
                entityDefId = c.entityDefId,
                attributeDefId = c.attributeDefId,
                cmd = c.cmd
            )
        )
    }

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    override fun dispatch(cmd: ModelCmd) {
        ensureModelExists(cmd.modelId)
        when (cmd) {
            is ModelCmd.CreateType -> createType(cmd)
            is ModelCmd.UpdateType -> updateType(cmd)
            is ModelCmd.DeleteType -> deleteType(cmd)
            is ModelCmd.CreateEntityDef -> createEntityDef(cmd)
            is ModelCmd.UpdateEntityDef -> updateEntityDef(cmd)
            is ModelCmd.DeleteEntityDef -> deleteEntityDef(cmd)
            is ModelCmd.CreateEntityDefAttributeDef -> createEntityDefAttributeDef(cmd)
            is ModelCmd.UpdateEntityDefAttributeDef -> updateEntityDefAttributeDef(cmd)
            is ModelCmd.DeleteEntityDefAttributeDef -> deleteEntityDefAttributeDef(cmd)
            is ModelCmd.CreateRelationshipDef -> createRelationshipDef(cmd)
            is ModelCmd.CreateRelationshipAttributeDef -> createRelatinoshipAttributeDef(cmd)
            is ModelCmd.UpdateRelationshipDef -> updateRelationshipDef(cmd)
            is ModelCmd.DeleteRelationshipDef -> deleteRelationshipDef(cmd)
            is ModelCmd.UpdateRelationshipAttributeDef -> updateRelationshipAttributeDef(cmd)
            is ModelCmd.DeleteRelationshipAttributeDef -> deleteRelationshipAttributeDef(cmd)

        }
        return auditor.onCmdProcessed(cmd)
    }

    private fun deleteRelationshipAttributeDef(cmd: ModelCmd.DeleteRelationshipAttributeDef) {
        findModelById(cmd.modelId).findRelationshipDef(cmd.relationshipDefId)
            .ensureAttributeDefExists(cmd.attributeDefId)
        storage.dispatch(
            ModelRepositoryCmd.DeleteRelationshipAttributeDef(
                modelId = cmd.modelId,
                relationshipDefId = cmd.relationshipDefId,
                attributeDefId = cmd.attributeDefId
            )
        )
    }

    private fun updateRelationshipAttributeDef(cmd: ModelCmd.UpdateRelationshipAttributeDef) {
        findModelById(cmd.modelId).ensureRelationshipExists(cmd.relationshipDefId)
            .ensureAttributeDefExists(cmd.attributeDefId)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipAttributeDef(
                modelId = cmd.modelId,
                relationshipDefId = cmd.relationshipDefId,
                attributeDefId = cmd.attributeDefId,
                cmd = cmd.cmd
            )
        )
    }

    private fun deleteRelationshipDef(cmd: ModelCmd.DeleteRelationshipDef) {
        findModelById(cmd.modelId).ensureRelationshipExists(cmd.relationshipDefId)
        storage.dispatch(
            ModelRepositoryCmd.DeleteRelationshipDef(
                modelId = cmd.modelId,
                relationshipDefId = cmd.relationshipDefId
            )
        )
    }

    private fun updateRelationshipDef(cmd: ModelCmd.UpdateRelationshipDef) {
        findModelById(cmd.modelId).ensureRelationshipExists(cmd.relationshipDefId);
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipDef(
                modelId = cmd.modelId,
                relationshipDefId = cmd.relationshipDefId,
                cmd = cmd.cmd
            )
        )
    }

    private fun createRelatinoshipAttributeDef(cmd: ModelCmd.CreateRelationshipAttributeDef) {
        val exists = findModelById(cmd.modelId).findRelationshipDef(cmd.relationshipDefId)
            .findAttributeDefOptional(cmd.attr.id)
        if (exists != null) {
            throw RelationshipDuplicateAttributeException(cmd.modelId, cmd.relationshipDefId, cmd.attr.id)
        }
        findModelById(cmd.modelId).ensureTypeExists(cmd.attr.type)
        storage.dispatch(
            ModelRepositoryCmd.CreateRelationshipAttributeDef(
                modelId = cmd.modelId,
                attr = cmd.attr,
                relationshipDefId = cmd.relationshipDefId
            )
        )
    }

    private fun createRelationshipDef(cmd: ModelCmd.CreateRelationshipDef) {
        if (findModelById(cmd.modelId).findRelationshipDefOptional(cmd.initializer.id) != null)
            throw RelationshipDuplicateIdException(cmd.modelId, cmd.initializer.id)
        val duplicateRoleIds =
            cmd.initializer.roles.groupBy { it.id }.mapValues { it.value.size }.filter { it.value > 1 }
        if (duplicateRoleIds.isNotEmpty()) {
            throw RelationshipDuplicateRoleIdException(duplicateRoleIds.keys)
        }
        storage.dispatch(ModelRepositoryCmd.CreateRelationshipDef(modelId = cmd.modelId, initializer = cmd.initializer))
    }

    fun ensureModelExists(modelId: ModelId) {
        if (!storage.existsModelById(modelId)) throw ModelNotFoundException(modelId)
    }

    fun findModelById(modelId: ModelId): Model {
        return storage.findModelByIdOptional(modelId) ?: throw ModelNotFoundException(modelId)
    }

}
