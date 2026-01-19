package io.medatarun.model.internal

import io.medatarun.model.domain.*
import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.ports.exposed.*
import io.medatarun.model.ports.needs.ModelRepositoryCmd
import io.medatarun.model.ports.needs.ModelStorages

class ModelCmdsImpl(
    val storage: ModelStorages,
    val auditor: ModelAuditor
) : ModelCmds {

    private fun createModel(cmd: ModelCmd.CreateModel) {
        val model = ModelInMemory(
            id = cmd.modelKey,
            name = cmd.name,
            description = cmd.description,
            version = cmd.version,
            origin = ModelOrigin.Manual,
            types = emptyList(),
            entityDefs = emptyList(),
            relationshipDefs = emptyList(),
            documentationHome = null,
            hashtags = emptyList(),
        )
        storage.dispatch(ModelRepositoryCmd.CreateModel(model), cmd.repositoryRef)
    }

    private fun copyModel(cmd: ModelCmd.CopyModel) {
        val model = storage.findModelByIdOptional(cmd.modelKey) ?: throw ModelNotFoundException(cmd.modelKey)
        val existing = storage.findModelByIdOptional(cmd.modelNewKey)
        if (existing != null) throw ModelDuplicateIdException(cmd.modelNewKey)
        val next = ModelInMemory.of(model).copy(id = cmd.modelNewKey)
        storage.dispatch(ModelRepositoryCmd.CreateModel(next), cmd.repositoryRef)
    }

    private fun importModel(cmd: ModelCmd.ImportModel) {
        val existing = storage.findModelByIdOptional(cmd.model.id)
        if (existing != null) throw ModelDuplicateIdException(cmd.model.id)
        storage.dispatch(ModelRepositoryCmd.CreateModel(cmd.model), cmd.repositoryRef)
    }

    private fun deleteModel(cmd: ModelCmd.DeleteModel) {
        storage.dispatch(ModelRepositoryCmd.DeleteModel(cmd.modelKey))
    }

    private fun updateModelName(cmd: ModelCmd.UpdateModelName) {
        storage.dispatch(ModelRepositoryCmd.UpdateModelName(cmd.modelKey, cmd.name))
    }

    private fun updateModelDescription(cmd: ModelCmd.UpdateModelDescription) {
        storage.dispatch(ModelRepositoryCmd.UpdateModelDescription(cmd.modelKey, cmd.description))
    }

    private fun updateModelVersion(cmd: ModelCmd.UpdateModelVersion) {
        storage.dispatch(ModelRepositoryCmd.UpdateModelVersion(cmd.modelKey, cmd.version))
    }

    private fun updateDocumentationHome(cmd: ModelCmd.UpdateModelDocumentationHome) {
        storage.dispatch(ModelRepositoryCmd.UpdateModelDocumentationHome(cmd.modelKey, cmd.url))
    }

    private fun updateModelHashtagAdd(cmd: ModelCmd.UpdateModelHashtagAdd) {
        storage.dispatch(ModelRepositoryCmd.UpdateModelHashtagAdd(cmd.modelKey, cmd.hashtag))
    }

    private fun updateModelHashtagDelete(cmd: ModelCmd.UpdateModelHashtagDelete) {
        storage.dispatch(ModelRepositoryCmd.UpdateModelHashtagDelete(cmd.modelKey, cmd.hashtag))
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Types
    // -----------------------------------------------------------------------------------------------------------------

    private fun createType(c: ModelCmd.CreateType) {
        // Can not create a type if another has the same type
        val model = storage.findModelByIdOptional(c.modelKey) ?: throw ModelNotFoundException(c.modelKey)
        val existing = model.findTypeOptional(c.initializer.id)
        if (existing != null) throw TypeCreateDuplicateException(c.modelKey, c.initializer.id)
        storage.dispatch(ModelRepositoryCmd.CreateType(c.modelKey, c.initializer))
    }

    private fun updateType(c: ModelCmd.UpdateType) {
        val model = storage.findModelByIdOptional(c.modelKey) ?: throw ModelNotFoundException(c.modelKey)
        model.findTypeOptional(c.typeId) ?: throw TypeNotFoundException(c.modelKey, c.typeId)
        storage.dispatch(ModelRepositoryCmd.UpdateType(c.modelKey, c.typeId, c.cmd))
    }

    private fun deleteType(c: ModelCmd.DeleteType) {
        // Can not delete type used in any entity
        val model = storage.findModelByIdOptional(c.modelKey) ?: throw ModelNotFoundException(c.modelKey)
        val used = model.entityDefs.any { entityDef -> entityDef.attributes.any { attr -> attr.type == c.typeId } }
        if (used) throw ModelTypeDeleteUsedException(c.typeId)
        model.findTypeOptional(c.typeId) ?: throw TypeNotFoundException(c.modelKey, c.typeId)
        storage.dispatch(ModelRepositoryCmd.DeleteType(c.modelKey, c.typeId))
    }
    // -----------------------------------------------------------------------------------------------------------------
    // Entities
    // -----------------------------------------------------------------------------------------------------------------

    private fun updateEntityDef(c: ModelCmd.UpdateEntityDef) {
        ensureModelExists(c.modelKey)
        val model = storage.findModelById(c.modelKey)
        model.findEntityDef(c.entityKey)
        if (c.cmd is EntityDefUpdateCmd.Id) {
            if (model.entityDefs.any { it.id == c.cmd.value && it.id != c.entityKey }) {
                throw UpdateEntityDefIdDuplicateIdException(c.entityKey)
            }
        }
        storage.dispatch(ModelRepositoryCmd.UpdateEntityDef(c.modelKey, c.entityKey, c.cmd))
    }

    private fun updateEntityDefHashtagAdd(cmd: ModelCmd.UpdateEntityDefHashtagAdd) {
        ensureModelExists(cmd.modelKey)
        val model = storage.findModelById(cmd.modelKey)
        model.findEntityDef(cmd.entityKey)
        storage.dispatch(ModelRepositoryCmd.UpdateEntityDefHashtagAdd(cmd.modelKey, cmd.entityKey, cmd.hashtag))
    }

    private fun updateEntityDefHashtagDelete(cmd: ModelCmd.UpdateEntityDefHashtagDelete) {
        ensureModelExists(cmd.modelKey)
        val model = storage.findModelById(cmd.modelKey)
        model.findEntityDef(cmd.entityKey)
        storage.dispatch(ModelRepositoryCmd.UpdateEntityDefHashtagDelete(cmd.modelKey, cmd.entityKey, cmd.hashtag))
    }


    private fun createEntityDef(c: ModelCmd.CreateEntityDef) {

        findModelById(c.modelKey).ensureTypeExists(c.entityDefInitializer.identityAttribute.type)
        storage.dispatch(
            ModelRepositoryCmd.CreateEntityDef(
                c.modelKey, EntityDefInMemory(
                    id = c.entityDefInitializer.entityKey,
                    name = c.entityDefInitializer.name,
                    description = c.entityDefInitializer.description,
                    identifierAttributeKey = c.entityDefInitializer.identityAttribute.attributeKey,
                    origin = EntityOrigin.Manual,
                    documentationHome = c.entityDefInitializer.documentationHome,
                    hashtags = emptyList(),
                    attributes = listOf(
                        AttributeDefInMemory(
                            id = c.entityDefInitializer.identityAttribute.attributeKey,
                            name = c.entityDefInitializer.identityAttribute.name,
                            description = c.entityDefInitializer.identityAttribute.description,
                            type = c.entityDefInitializer.identityAttribute.type,
                            optional = false, // because it's identity, can never be optional
                            hashtags = emptyList()
                        )
                    )
                )
            )
        )
    }

    private fun deleteEntityDef(c: ModelCmd.DeleteEntityDef) {
        storage.dispatch(ModelRepositoryCmd.DeleteEntityDef(modelKey = c.modelKey, entityKey = c.entityKey))
    }

    private fun createEntityDefAttributeDef(c: ModelCmd.CreateEntityDefAttributeDef) {
        val e = findModelById(c.modelKey).findEntityDef(c.entityKey)
        if (e.hasAttributeDef(c.attributeDefInitializer.attributeKey)) throw CreateAttributeDefDuplicateIdException(
            c.entityKey,
            c.attributeDefInitializer.attributeKey
        )
        findModelById(c.modelKey).ensureTypeExists(c.attributeDefInitializer.type)
        storage.dispatch(
            ModelRepositoryCmd.CreateEntityDefAttributeDef(
                modelKey = c.modelKey,
                entityKey = c.entityKey,
                attributeDef = AttributeDefInMemory(
                    id = c.attributeDefInitializer.attributeKey,
                    name = c.attributeDefInitializer.name,
                    description = c.attributeDefInitializer.description,
                    type = c.attributeDefInitializer.type,
                    optional = c.attributeDefInitializer.optional,
                    hashtags = emptyList()
                )
            )
        )
    }

    private fun deleteEntityDefAttributeDef(c: ModelCmd.DeleteEntityDefAttributeDef) {
        val entity = findModelById(c.modelKey).findEntityDef(c.entityKey)
        if (entity.identifierAttributeKey == c.attributeKey)
            throw DeleteAttributeIdentifierException(c.modelKey, c.entityKey, c.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.DeleteEntityDefAttributeDef(
                modelKey = c.modelKey,
                entityKey = c.entityKey,
                attributeKey = c.attributeKey
            )
        )
    }

    private fun updateEntityDefAttributeDef(c: ModelCmd.UpdateEntityDefAttributeDef) {
        val model = findModelById(c.modelKey)
        val entity = model.findEntityDef(c.entityKey)
        entity.ensureAttributeDefExists(c.attributeKey)

        // TODO how do we ensure transactions here ?


        if (c.cmd is AttributeDefUpdateCmd.Key) {
            // We can not have two attributes with the same id
            if (entity.attributes.any { it.id == c.cmd.value && it.id != c.attributeKey }) {
                throw UpdateAttributeDefDuplicateIdException(c.entityKey, c.attributeKey)
            }
            // If user wants to rename the Entity's identity attribute, we must rename in entity
            // as well as the attribute's id, then apply changes on entity
            if (entity.identifierAttributeKey == c.attributeKey) {
                storage.dispatch(
                    ModelRepositoryCmd.UpdateEntityDef(
                        modelKey = c.modelKey,
                        entityKey = c.entityKey,
                        cmd = EntityDefUpdateCmd.IdentifierAttribute(c.cmd.value)
                    )
                )
            }
        } else if (c.cmd is AttributeDefUpdateCmd.Type) {
            // Attribute type shall exist when updating types
            findModelById(c.modelKey).ensureTypeExists(c.cmd.value)
        }

        // Apply changes on attribute
        storage.dispatch(
            ModelRepositoryCmd.UpdateEntityDefAttributeDef(
                modelKey = c.modelKey,
                entityKey = c.entityKey,
                attributeKey = c.attributeKey,
                cmd = c.cmd
            )
        )
    }

    private fun updateEntityDefAttributeDefHashtagAdd(c: ModelCmd.UpdateEntityDefAttributeDefHashtagAdd) {
        val model = findModelById(c.modelKey)
        val entity = model.findEntityDef(c.entityKey)
        entity.ensureAttributeDefExists(c.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateEntityDefAttributeDefHashtagAdd(
                modelKey = c.modelKey,
                entityKey = c.entityKey,
                attributeKey = c.attributeKey,
                hashtag = c.hashtag
            )
        )
    }

    private fun updateEntityDefAttributeDefHashtagDelete(c: ModelCmd.UpdateEntityDefAttributeDefHashtagDelete) {
        val model = findModelById(c.modelKey)
        val entity = model.findEntityDef(c.entityKey)
        entity.ensureAttributeDefExists(c.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateEntityDefAttributeDefHashtagDelete(
                modelKey = c.modelKey,
                entityKey = c.entityKey,
                attributeKey = c.attributeKey,
                hashtag = c.hashtag
            )
        )
    }

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    override fun dispatch(cmd: ModelCmd) {
        if (cmd is ModelCmdOnModel) ensureModelExists(cmd.modelKey)
        when (cmd) {
            is ModelCmd.CreateModel -> createModel(cmd)
            is ModelCmd.CopyModel -> copyModel(cmd)
            is ModelCmd.ImportModel -> importModel(cmd)
            is ModelCmd.UpdateModelDescription -> updateModelDescription(cmd)
            is ModelCmd.UpdateModelName -> updateModelName(cmd)
            is ModelCmd.UpdateModelVersion -> updateModelVersion(cmd)
            is ModelCmd.UpdateModelDocumentationHome -> updateDocumentationHome(cmd)
            is ModelCmd.UpdateModelHashtagAdd -> updateModelHashtagAdd(cmd)
            is ModelCmd.UpdateModelHashtagDelete -> updateModelHashtagDelete(cmd)
            is ModelCmd.DeleteModel -> deleteModel(cmd)
            is ModelCmd.CreateType -> createType(cmd)
            is ModelCmd.UpdateType -> updateType(cmd)
            is ModelCmd.DeleteType -> deleteType(cmd)
            is ModelCmd.CreateEntityDef -> createEntityDef(cmd)
            is ModelCmd.UpdateEntityDef -> updateEntityDef(cmd)
            is ModelCmd.UpdateEntityDefHashtagAdd -> updateEntityDefHashtagAdd(cmd)
            is ModelCmd.UpdateEntityDefHashtagDelete -> updateEntityDefHashtagDelete(cmd)
            is ModelCmd.DeleteEntityDef -> deleteEntityDef(cmd)
            is ModelCmd.CreateEntityDefAttributeDef -> createEntityDefAttributeDef(cmd)
            is ModelCmd.UpdateEntityDefAttributeDef -> updateEntityDefAttributeDef(cmd)
            is ModelCmd.UpdateEntityDefAttributeDefHashtagAdd -> updateEntityDefAttributeDefHashtagAdd(cmd)
            is ModelCmd.UpdateEntityDefAttributeDefHashtagDelete -> updateEntityDefAttributeDefHashtagDelete(cmd)
            is ModelCmd.DeleteEntityDefAttributeDef -> deleteEntityDefAttributeDef(cmd)
            is ModelCmd.CreateRelationshipDef -> createRelationshipDef(cmd)
            is ModelCmd.CreateRelationshipAttributeDef -> createRelationshipAttributeDef(cmd)
            is ModelCmd.UpdateRelationshipDef -> updateRelationshipDef(cmd)
            is ModelCmd.UpdateRelationshipDefHashtagAdd -> updateRelationshipDefHashtagAdd(cmd)
            is ModelCmd.UpdateRelationshipDefHashtagDelete -> updateRelationshipDefHashtagDelete(cmd)
            is ModelCmd.DeleteRelationshipDef -> deleteRelationshipDef(cmd)
            is ModelCmd.UpdateRelationshipAttributeDef -> updateRelationshipAttributeDef(cmd)
            is ModelCmd.UpdateRelationshipAttributeDefHashtagAdd -> updateRelationshipAttributeDefHashtagAdd(cmd)
            is ModelCmd.UpdateRelationshipAttributeDefHashtagDelete -> updateRelationshipAttributeDefHashtagDelete(cmd)
            is ModelCmd.DeleteRelationshipAttributeDef -> deleteRelationshipAttributeDef(cmd)

        }
        return auditor.onCmdProcessed(cmd)
    }

    private fun deleteRelationshipAttributeDef(cmd: ModelCmd.DeleteRelationshipAttributeDef) {
        findModelById(cmd.modelKey).findRelationshipDef(cmd.relationshipKey)
            .ensureAttributeDefExists(cmd.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.DeleteRelationshipAttributeDef(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                attributeKey = cmd.attributeKey
            )
        )
    }

    private fun updateRelationshipAttributeDef(cmd: ModelCmd.UpdateRelationshipAttributeDef) {
        findModelById(cmd.modelKey).ensureRelationshipExists(cmd.relationshipKey)
            .ensureAttributeDefExists(cmd.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipAttributeDef(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                attributeKey = cmd.attributeKey,
                cmd = cmd.cmd
            )
        )
    }

    private fun updateRelationshipAttributeDefHashtagAdd(cmd: ModelCmd.UpdateRelationshipAttributeDefHashtagAdd) {
        findModelById(cmd.modelKey).ensureRelationshipExists(cmd.relationshipKey)
            .ensureAttributeDefExists(cmd.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipAttributeDefHashtagAdd(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                attributeKey = cmd.attributeKey,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun updateRelationshipAttributeDefHashtagDelete(cmd: ModelCmd.UpdateRelationshipAttributeDefHashtagDelete) {
        findModelById(cmd.modelKey).ensureRelationshipExists(cmd.relationshipKey)
            .ensureAttributeDefExists(cmd.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipAttributeDefHashtagDelete(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                attributeKey = cmd.attributeKey,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun deleteRelationshipDef(cmd: ModelCmd.DeleteRelationshipDef) {
        findModelById(cmd.modelKey).ensureRelationshipExists(cmd.relationshipKey)
        storage.dispatch(
            ModelRepositoryCmd.DeleteRelationshipDef(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey
            )
        )
    }

    private fun updateRelationshipDef(cmd: ModelCmd.UpdateRelationshipDef) {
        findModelById(cmd.modelKey).ensureRelationshipExists(cmd.relationshipKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipDef(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                cmd = cmd.cmd
            )
        )
    }

    private fun updateRelationshipDefHashtagAdd(cmd: ModelCmd.UpdateRelationshipDefHashtagAdd) {
        findModelById(cmd.modelKey).ensureRelationshipExists(cmd.relationshipKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipDefHashtagAdd(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun updateRelationshipDefHashtagDelete(cmd: ModelCmd.UpdateRelationshipDefHashtagDelete) {
        findModelById(cmd.modelKey).ensureRelationshipExists(cmd.relationshipKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipDefHashtagDelete(
                modelKey = cmd.modelKey,
                relationshipKey = cmd.relationshipKey,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun createRelationshipAttributeDef(cmd: ModelCmd.CreateRelationshipAttributeDef) {
        val exists = findModelById(cmd.modelKey).findRelationshipDef(cmd.relationshipKey)
            .findAttributeDefOptional(cmd.attr.id)
        if (exists != null) {
            throw RelationshipDuplicateAttributeException(cmd.modelKey, cmd.relationshipKey, cmd.attr.id)
        }
        findModelById(cmd.modelKey).ensureTypeExists(cmd.attr.type)
        storage.dispatch(
            ModelRepositoryCmd.CreateRelationshipAttributeDef(
                modelKey = cmd.modelKey,
                attr = cmd.attr,
                relationshipKey = cmd.relationshipKey
            )
        )
    }


    private fun createRelationshipDef(cmd: ModelCmd.CreateRelationshipDef) {
        if (findModelById(cmd.modelKey).findRelationshipDefOptional(cmd.initializer.id) != null)
            throw RelationshipDuplicateIdException(cmd.modelKey, cmd.initializer.id)
        val duplicateRoleIds =
            cmd.initializer.roles.groupBy { it.id }.mapValues { it.value.size }.filter { it.value > 1 }
        if (duplicateRoleIds.isNotEmpty()) {
            throw RelationshipDuplicateRoleIdException(duplicateRoleIds.keys)
        }
        storage.dispatch(ModelRepositoryCmd.CreateRelationshipDef(modelKey = cmd.modelKey, initializer = cmd.initializer))
    }

    fun ensureModelExists(modelKey: ModelKey) {
        if (!storage.existsModelById(modelKey)) throw ModelNotFoundException(modelKey)
    }

    fun findModelById(modelKey: ModelKey): Model {
        return storage.findModelByIdOptional(modelKey) ?: throw ModelNotFoundException(modelKey)
    }

}
