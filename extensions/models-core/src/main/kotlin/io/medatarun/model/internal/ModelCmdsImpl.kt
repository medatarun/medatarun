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

    private fun findModelById(id: ModelId): Model {
        return storage.findModelByIdOptional(id) ?: throw ModelNotFoundByIdException(id)
    }

    private fun findModelByKey(key: ModelKey): Model {
        return storage.findModelByKeyOptional(key) ?: throw ModelNotFoundByKeyException(key)
    }

    private fun createModel(cmd: ModelCmd.CreateModel) {
        val model = ModelInMemory(
            id = ModelId.generate(),
            key = cmd.modelKey,
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
        val model = storage.findModelByKeyOptional(cmd.modelKey) ?: throw ModelNotFoundByKeyException(cmd.modelKey)
        val existing = storage.findModelByKeyOptional(cmd.modelNewKey)
        if (existing != null) throw ModelDuplicateIdException(cmd.modelNewKey)
        val next = ModelInMemory.of(model).copy(key = cmd.modelNewKey)
        storage.dispatch(ModelRepositoryCmd.CreateModel(next), cmd.repositoryRef)
    }

    private fun importModel(cmd: ModelCmd.ImportModel) {
        val existing = storage.findModelByKeyOptional(cmd.model.key)
        if (existing != null) throw ModelDuplicateIdException(cmd.model.key)
        storage.dispatch(ModelRepositoryCmd.CreateModel(cmd.model), cmd.repositoryRef)
    }


    private fun deleteModel(cmd: ModelCmd.DeleteModel) {
        val model = findModelByKey(cmd.modelKey)
        storage.dispatch(ModelRepositoryCmd.DeleteModel(model.id))
    }

    private fun updateModelName(cmd: ModelCmd.UpdateModelName) {
        val model = findModelByKey(cmd.modelKey)
        storage.dispatch(ModelRepositoryCmd.UpdateModelName(model.id, cmd.name))
    }

    private fun updateModelDescription(cmd: ModelCmd.UpdateModelDescription) {
        val model = findModelByKey(cmd.modelKey)
        storage.dispatch(ModelRepositoryCmd.UpdateModelDescription(model.id, cmd.description))
    }

    private fun updateModelVersion(cmd: ModelCmd.UpdateModelVersion) {
        val model = findModelByKey(cmd.modelKey)
        storage.dispatch(ModelRepositoryCmd.UpdateModelVersion(model.id, cmd.version))
    }

    private fun updateDocumentationHome(cmd: ModelCmd.UpdateModelDocumentationHome) {
        val model = findModelByKey(cmd.modelKey)
        storage.dispatch(ModelRepositoryCmd.UpdateModelDocumentationHome(model.id, cmd.url))
    }

    private fun updateModelHashtagAdd(cmd: ModelCmd.UpdateModelHashtagAdd) {
        val model = findModelByKey(cmd.modelKey)
        storage.dispatch(ModelRepositoryCmd.UpdateModelHashtagAdd(model.id, cmd.hashtag))
    }

    private fun updateModelHashtagDelete(cmd: ModelCmd.UpdateModelHashtagDelete) {
        val model = findModelByKey(cmd.modelKey)
        storage.dispatch(ModelRepositoryCmd.UpdateModelHashtagDelete(model.id, cmd.hashtag))
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Types
    // -----------------------------------------------------------------------------------------------------------------

    private fun createType(c: ModelCmd.CreateType) {
        // Cannot create a type if another type already has the same key in the model
        val model = storage.findModelByKeyOptional(c.modelKey) ?: throw ModelNotFoundByKeyException(c.modelKey)
        val existing = model.findTypeOptional(c.initializer.id)
        if (existing != null) throw TypeCreateDuplicateException(c.modelKey, c.initializer.id)
        storage.dispatch(ModelRepositoryCmd.CreateType(model.id, c.initializer))
    }

    private fun updateType(c: ModelCmd.UpdateType) {
        val model = storage.findModelByKeyOptional(c.modelKey) ?: throw ModelNotFoundByKeyException(c.modelKey)
        model.findTypeOptional(c.typeId) ?: throw TypeNotFoundException(c.modelKey, c.typeId)
        storage.dispatch(ModelRepositoryCmd.UpdateType(model.id, c.typeId, c.cmd))
    }

    private fun deleteType(c: ModelCmd.DeleteType) {
        // Can not delete type used in any entity
        val model = storage.findModelByKeyOptional(c.modelKey) ?: throw ModelNotFoundByKeyException(c.modelKey)
        val used = model.entityDefs.any { entityDef -> entityDef.attributes.any { attr -> attr.type == c.typeId } }
        if (used) throw ModelTypeDeleteUsedException(c.typeId)
        model.findTypeOptional(c.typeId) ?: throw TypeNotFoundException(c.modelKey, c.typeId)
        storage.dispatch(ModelRepositoryCmd.DeleteType(model.id, c.typeId))
    }
    // -----------------------------------------------------------------------------------------------------------------
    // Entities
    // -----------------------------------------------------------------------------------------------------------------

    private fun updateEntityDef(c: ModelCmd.UpdateEntityDef) {
        ensureModelExists(c.modelKey)
        val model = storage.findModelByKey(c.modelKey)
        model.findEntityDef(c.entityKey)
        if (c.cmd is EntityDefUpdateCmd.Id) {
            if (model.entityDefs.any { it.key == c.cmd.value && it.key != c.entityKey }) {
                throw UpdateEntityDefIdDuplicateIdException(c.entityKey)
            }
        }
        storage.dispatch(ModelRepositoryCmd.UpdateEntityDef(model.id, c.entityKey, c.cmd))
    }

    private fun updateEntityDefHashtagAdd(cmd: ModelCmd.UpdateEntityDefHashtagAdd) {
        ensureModelExists(cmd.modelKey)
        val model = storage.findModelByKey(cmd.modelKey)
        model.findEntityDef(cmd.entityKey)
        storage.dispatch(ModelRepositoryCmd.UpdateEntityDefHashtagAdd(model.id, cmd.entityKey, cmd.hashtag))
    }

    private fun updateEntityDefHashtagDelete(cmd: ModelCmd.UpdateEntityDefHashtagDelete) {
        ensureModelExists(cmd.modelKey)
        val model = storage.findModelByKey(cmd.modelKey)
        model.findEntityDef(cmd.entityKey)
        storage.dispatch(ModelRepositoryCmd.UpdateEntityDefHashtagDelete(model.id, cmd.entityKey, cmd.hashtag))
    }


    private fun createEntityDef(c: ModelCmd.CreateEntityDef) {

        val model = findModelByKey(c.modelKey)
        model.ensureTypeExists(c.entityDefInitializer.identityAttribute.type)
        storage.dispatch(
            ModelRepositoryCmd.CreateEntityDef(
                model.id,
                EntityDefInMemory(
                    id = EntityId.generate(),
                    key = c.entityDefInitializer.entityKey,
                    name = c.entityDefInitializer.name,
                    description = c.entityDefInitializer.description,
                    identifierAttributeKey = c.entityDefInitializer.identityAttribute.attributeKey,
                    origin = EntityOrigin.Manual,
                    documentationHome = c.entityDefInitializer.documentationHome,
                    hashtags = emptyList(),
                    attributes = listOf(
                        AttributeDefInMemory(
                            id = AttributeId.generate(),
                            key = c.entityDefInitializer.identityAttribute.attributeKey,
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
        val model = findModelByKey(c.modelKey)
        storage.dispatch(ModelRepositoryCmd.DeleteEntityDef(model.id, c.entityKey))
    }

    private fun createEntityDefAttributeDef(c: ModelCmd.CreateEntityDefAttributeDef) {
        val e = findModelByKey(c.modelKey).findEntityDef(c.entityKey)
        if (e.hasAttributeDef(c.attributeDefInitializer.attributeKey)) throw CreateAttributeDefDuplicateIdException(
            c.entityKey,
            c.attributeDefInitializer.attributeKey
        )
        val model = findModelByKey(c.modelKey)
        model.ensureTypeExists(c.attributeDefInitializer.type)
        storage.dispatch(
            ModelRepositoryCmd.CreateEntityDefAttributeDef(
                modelId = model.id,
                entityKey = c.entityKey,
                attributeDef = AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = c.attributeDefInitializer.attributeKey,
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
        val model = findModelByKey(c.modelKey)
        val entity = model.findEntityDef(c.entityKey)
        if (entity.identifierAttributeKey == c.attributeKey)
            throw DeleteAttributeIdentifierException(c.modelKey, c.entityKey, c.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.DeleteEntityDefAttributeDef(
                modelId = model.id,
                entityKey = c.entityKey,
                attributeKey = c.attributeKey
            )
        )
    }

    private fun updateEntityDefAttributeDef(c: ModelCmd.UpdateEntityDefAttributeDef) {
        val model = findModelByKey(c.modelKey)
        val entity = model.findEntityDef(c.entityKey)
        entity.ensureAttributeDefExists(c.attributeKey)

        // TODO how do we ensure transactions here ?


        if (c.cmd is AttributeDefUpdateCmd.Key) {
            // We can not have two attributes with the same id
            if (entity.attributes.any { it.key == c.cmd.value && it.key != c.attributeKey }) {
                throw UpdateAttributeDefDuplicateIdException(c.entityKey, c.attributeKey)
            }
            // If user wants to rename the Entity's identity attribute, we must rename in entity
            // as well as the attribute's id, then apply changes on entity
            if (entity.identifierAttributeKey == c.attributeKey) {
                storage.dispatch(
                    ModelRepositoryCmd.UpdateEntityDef(
                        modelId = model.id,
                        entityKey = c.entityKey,
                        cmd = EntityDefUpdateCmd.IdentifierAttribute(c.cmd.value)
                    )
                )
            }
        } else if (c.cmd is AttributeDefUpdateCmd.Type) {
            // Attribute type shall exist when updating types
            findModelByKey(c.modelKey).ensureTypeExists(c.cmd.value)
        }

        // Apply changes on attribute
        storage.dispatch(
            ModelRepositoryCmd.UpdateEntityDefAttributeDef(
                modelId = model.id,
                entityKey = c.entityKey,
                attributeKey = c.attributeKey,
                cmd = c.cmd
            )
        )
    }

    private fun updateEntityDefAttributeDefHashtagAdd(c: ModelCmd.UpdateEntityDefAttributeDefHashtagAdd) {
        val model = findModelByKey(c.modelKey)
        val entity = model.findEntityDef(c.entityKey)
        entity.ensureAttributeDefExists(c.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateEntityDefAttributeDefHashtagAdd(
                modelId = model.id,
                entityKey = c.entityKey,
                attributeKey = c.attributeKey,
                hashtag = c.hashtag
            )
        )
    }

    private fun updateEntityDefAttributeDefHashtagDelete(c: ModelCmd.UpdateEntityDefAttributeDefHashtagDelete) {
        val model = findModelByKey(c.modelKey)
        val entity = model.findEntityDef(c.entityKey)
        entity.ensureAttributeDefExists(c.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateEntityDefAttributeDefHashtagDelete(
                modelId = model.id,
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
        val model = findModelByKey(cmd.modelKey)
        model.findRelationshipDef(cmd.relationshipKey)
            .ensureAttributeDefExists(cmd.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.DeleteRelationshipAttributeDef(
                modelId = model.id,
                relationshipKey = cmd.relationshipKey,
                attributeKey = cmd.attributeKey
            )
        )
    }

    private fun updateRelationshipAttributeDef(cmd: ModelCmd.UpdateRelationshipAttributeDef) {
        val model = findModelByKey(cmd.modelKey)
        model.ensureRelationshipExists(cmd.relationshipKey)
            .ensureAttributeDefExists(cmd.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipAttributeDef(
                modelId = model.id,
                relationshipKey = cmd.relationshipKey,
                attributeKey = cmd.attributeKey,
                cmd = cmd.cmd
            )
        )
    }

    private fun updateRelationshipAttributeDefHashtagAdd(cmd: ModelCmd.UpdateRelationshipAttributeDefHashtagAdd) {
        val model = findModelByKey(cmd.modelKey)
        model.ensureRelationshipExists(cmd.relationshipKey)
            .ensureAttributeDefExists(cmd.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipAttributeDefHashtagAdd(
                modelId = model.id,
                relationshipKey = cmd.relationshipKey,
                attributeKey = cmd.attributeKey,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun updateRelationshipAttributeDefHashtagDelete(cmd: ModelCmd.UpdateRelationshipAttributeDefHashtagDelete) {
        val model = findModelByKey(cmd.modelKey)
        model.ensureRelationshipExists(cmd.relationshipKey)
            .ensureAttributeDefExists(cmd.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipAttributeDefHashtagDelete(
                modelId = model.id,
                relationshipKey = cmd.relationshipKey,
                attributeKey = cmd.attributeKey,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun deleteRelationshipDef(cmd: ModelCmd.DeleteRelationshipDef) {
        val model = findModelByKey(cmd.modelKey)
        model.ensureRelationshipExists(cmd.relationshipKey)
        storage.dispatch(
            ModelRepositoryCmd.DeleteRelationshipDef(
                modelId = model.id,
                relationshipKey = cmd.relationshipKey
            )
        )
    }

    private fun updateRelationshipDef(cmd: ModelCmd.UpdateRelationshipDef) {
        val model = findModelByKey(cmd.modelKey)
        model.ensureRelationshipExists(cmd.relationshipKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipDef(
                modelId = model.id,
                relationshipKey = cmd.relationshipKey,
                cmd = cmd.cmd
            )
        )
    }

    private fun updateRelationshipDefHashtagAdd(cmd: ModelCmd.UpdateRelationshipDefHashtagAdd) {
        val model = findModelByKey(cmd.modelKey)
        model.ensureRelationshipExists(cmd.relationshipKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipDefHashtagAdd(
                modelId = model.id,
                relationshipKey = cmd.relationshipKey,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun updateRelationshipDefHashtagDelete(cmd: ModelCmd.UpdateRelationshipDefHashtagDelete) {
        val model = findModelByKey(cmd.modelKey)
        model.ensureRelationshipExists(cmd.relationshipKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateRelationshipDefHashtagDelete(
                modelId = model.id,
                relationshipKey = cmd.relationshipKey,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun createRelationshipAttributeDef(cmd: ModelCmd.CreateRelationshipAttributeDef) {
        val model = findModelByKey(cmd.modelKey)
        val exists = model.findRelationshipDef(cmd.relationshipKey)
            .findAttributeDefOptional(cmd.attr.key)
        if (exists != null) {
            throw RelationshipDuplicateAttributeException(cmd.modelKey, cmd.relationshipKey, cmd.attr.key)
        }
        model.ensureTypeExists(cmd.attr.type)
        storage.dispatch(
            ModelRepositoryCmd.CreateRelationshipAttributeDef(
                modelId = model.id,
                attr = cmd.attr,
                relationshipKey = cmd.relationshipKey
            )
        )
    }


    private fun createRelationshipDef(cmd: ModelCmd.CreateRelationshipDef) {
        val model = findModelByKey(cmd.modelKey)
        if (model.findRelationshipDefOptional(cmd.initializer.key) != null)
            throw RelationshipDuplicateIdException(cmd.modelKey, cmd.initializer.key)
        val duplicateRoleIds =
            cmd.initializer.roles.groupBy { it.key }.mapValues { it.value.size }.filter { it.value > 1 }
        if (duplicateRoleIds.isNotEmpty()) {
            throw RelationshipDuplicateRoleIdException(duplicateRoleIds.keys)
        }
        storage.dispatch(
            ModelRepositoryCmd.CreateRelationshipDef(
                modelId = model.id,
                initializer = cmd.initializer
            )
        )
    }

    fun ensureModelExists(modelKey: ModelKey) {
        if (!storage.existsModelByKey(modelKey)) throw ModelNotFoundByKeyException(modelKey)
    }


}
