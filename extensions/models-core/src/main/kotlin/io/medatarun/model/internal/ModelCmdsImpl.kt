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

    override fun dispatch(cmd: ModelCmd) {
        if (cmd is ModelCmdOnModel) ensureModelExists(cmd.modelRef)
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

    private fun findModelById(id: ModelId): Model {
        return storage.findModelByIdOptional(id) ?: throw ModelNotFoundByIdException(id)
    }

    private fun findModelByKey(key: ModelKey): Model {
        return storage.findModelByKeyOptional(key) ?: throw ModelNotFoundByKeyException(key)
    }

    private fun findModelByRef(ref: ModelRef): Model {
        return when(ref) {
            is ModelRef.ByKey -> findModelByKey(ref.key)
            is ModelRef.ById -> findModelById(ref.id)
        }
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
        val model = findModelByRef(cmd.modelRef)
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
        val model = findModelByRef(cmd.modelRef)
        storage.dispatch(ModelRepositoryCmd.DeleteModel(model.id))
    }

    private fun updateModelName(cmd: ModelCmd.UpdateModelName) {
        val model = findModelByRef(cmd.modelRef)
        storage.dispatch(ModelRepositoryCmd.UpdateModelName(model.id, cmd.name))
    }

    private fun updateModelDescription(cmd: ModelCmd.UpdateModelDescription) {
        val model = findModelByRef(cmd.modelRef)
        storage.dispatch(ModelRepositoryCmd.UpdateModelDescription(model.id, cmd.description))
    }

    private fun updateModelVersion(cmd: ModelCmd.UpdateModelVersion) {
        val model = findModelByRef(cmd.modelRef)
        storage.dispatch(ModelRepositoryCmd.UpdateModelVersion(model.id, cmd.version))
    }

    private fun updateDocumentationHome(cmd: ModelCmd.UpdateModelDocumentationHome) {
        val model = findModelByRef(cmd.modelRef)
        storage.dispatch(ModelRepositoryCmd.UpdateModelDocumentationHome(model.id, cmd.url))
    }

    private fun updateModelHashtagAdd(cmd: ModelCmd.UpdateModelHashtagAdd) {
        val model = findModelByRef(cmd.modelRef)
        storage.dispatch(ModelRepositoryCmd.UpdateModelHashtagAdd(model.id, cmd.hashtag))
    }

    private fun updateModelHashtagDelete(cmd: ModelCmd.UpdateModelHashtagDelete) {
        val model = findModelByRef(cmd.modelRef)
        storage.dispatch(ModelRepositoryCmd.UpdateModelHashtagDelete(model.id, cmd.hashtag))
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Types
    // -----------------------------------------------------------------------------------------------------------------

    private fun createType(cmd: ModelCmd.CreateType) {
        // Cannot create a type if another type already has the same key in the model
        val model = findModelByRef(cmd.modelRef)
        val existing = model.findTypeOptional(cmd.initializer.id)
        if (existing != null) throw TypeCreateDuplicateException(model.key, cmd.initializer.id)
        storage.dispatch(ModelRepositoryCmd.CreateType(model.id, cmd.initializer))
    }

    private fun updateType(cmd: ModelCmd.UpdateType) {
        val model = findModelByRef(cmd.modelRef)
        val type = model.findTypeOptional(cmd.typeRef) ?: throw TypeNotFoundException(cmd.modelRef, cmd.typeRef)
        storage.dispatch(ModelRepositoryCmd.UpdateType(model.id, type.id, cmd.cmd))
    }

    private fun deleteType(cmd: ModelCmd.DeleteType) {
        // Cannot delete type used in any entity
        val model = findModelByRef(cmd.modelRef)
        val type = model.findTypeOptional(cmd.typeRef) ?: throw TypeNotFoundException(cmd.modelRef, cmd.typeRef)

        val used = model.entityDefs.any { entityDef -> entityDef.attributes.any { attr -> attr.type == type.key } }
        if (used) throw ModelTypeDeleteUsedException(type.key)

        storage.dispatch(ModelRepositoryCmd.DeleteType(model.id, type.id))
    }
    // -----------------------------------------------------------------------------------------------------------------
    // Entities
    // -----------------------------------------------------------------------------------------------------------------

    private fun updateEntityDef(cmd: ModelCmd.UpdateEntityDef) {
        val model = findModelByRef(cmd.modelRef)
        model.findEntityDef(cmd.entityKey)
        if (cmd.cmd is EntityDefUpdateCmd.Id) {
            if (model.entityDefs.any { it.key == cmd.cmd.value && it.key != cmd.entityKey }) {
                throw UpdateEntityDefIdDuplicateIdException(cmd.entityKey)
            }
        }
        storage.dispatch(ModelRepositoryCmd.UpdateEntityDef(model.id, cmd.entityKey, cmd.cmd))
    }

    private fun updateEntityDefHashtagAdd(cmd: ModelCmd.UpdateEntityDefHashtagAdd) {
        val model = findModelByRef(cmd.modelRef)
        model.findEntityDef(cmd.entityKey)
        storage.dispatch(ModelRepositoryCmd.UpdateEntityDefHashtagAdd(model.id, cmd.entityKey, cmd.hashtag))
    }

    private fun updateEntityDefHashtagDelete(cmd: ModelCmd.UpdateEntityDefHashtagDelete) {
        val model = findModelByRef(cmd.modelRef)
        model.findEntityDef(cmd.entityKey)
        storage.dispatch(ModelRepositoryCmd.UpdateEntityDefHashtagDelete(model.id, cmd.entityKey, cmd.hashtag))
    }


    private fun createEntityDef(c: ModelCmd.CreateEntityDef) {
        val model = findModelByRef(c.modelRef)
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
        val model = findModelByRef(c.modelRef)
        storage.dispatch(ModelRepositoryCmd.DeleteEntityDef(model.id, c.entityKey))
    }

    private fun createEntityDefAttributeDef(c: ModelCmd.CreateEntityDefAttributeDef) {
        val model = findModelByRef(c.modelRef)
        val e = model.findEntityDef(c.entityKey)
        if (e.hasAttributeDef(c.attributeDefInitializer.attributeKey)) throw CreateAttributeDefDuplicateIdException(
            c.entityKey,
            c.attributeDefInitializer.attributeKey
        )

        // Makes sure the type reference exists, even if now, the type is referenced by key only
        val typeRef = TypeRef.ByKey(c.attributeDefInitializer.type)
        model.findTypeOptional(typeRef) ?: throw TypeNotFoundException(c.modelRef, typeRef)

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

    private fun deleteEntityDefAttributeDef(cmd: ModelCmd.DeleteEntityDefAttributeDef) {
        val model = findModelByRef(cmd.modelRef)
        val entity = model.findEntityDef(cmd.entityKey)
        if (entity.identifierAttributeKey == cmd.attributeKey)
            throw DeleteAttributeIdentifierException(model.id, cmd.entityKey, cmd.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.DeleteEntityDefAttributeDef(
                modelId = model.id,
                entityKey = cmd.entityKey,
                attributeKey = cmd.attributeKey
            )
        )
    }

    private fun updateEntityDefAttributeDef(cmd: ModelCmd.UpdateEntityDefAttributeDef) {
        val model = findModelByRef(cmd.modelRef)
        val entity = model.findEntityDef(cmd.entityKey)
        entity.ensureAttributeDefExists(cmd.attributeKey)

        // TODO how do we ensure transactions here ?


        if (cmd.cmd is AttributeDefUpdateCmd.Key) {
            // We can not have two attributes with the same id
            if (entity.attributes.any { it.key == cmd.cmd.value && it.key != cmd.attributeKey }) {
                throw UpdateAttributeDefDuplicateIdException(cmd.entityKey, cmd.attributeKey)
            }
            // If user wants to rename the Entity's identity attribute, we must rename in entity
            // as well as the attribute's id, then apply changes on entity
            if (entity.identifierAttributeKey == cmd.attributeKey) {
                storage.dispatch(
                    ModelRepositoryCmd.UpdateEntityDef(
                        modelId = model.id,
                        entityKey = cmd.entityKey,
                        cmd = EntityDefUpdateCmd.IdentifierAttribute(cmd.cmd.value)
                    )
                )
            }
        } else if (cmd.cmd is AttributeDefUpdateCmd.Type) {
            // Attribute type shall exist when updating types
            val typeRef = TypeRef.ByKey(cmd.cmd.value)
            model.findTypeOptional(typeRef) ?: throw TypeNotFoundException(cmd.modelRef, typeRef)
        }

        // Apply changes on attribute
        storage.dispatch(
            ModelRepositoryCmd.UpdateEntityDefAttributeDef(
                modelId = model.id,
                entityKey = cmd.entityKey,
                attributeKey = cmd.attributeKey,
                cmd = cmd.cmd
            )
        )
    }

    private fun updateEntityDefAttributeDefHashtagAdd(cmd: ModelCmd.UpdateEntityDefAttributeDefHashtagAdd) {
        val model = findModelByRef(cmd.modelRef)
        val entity = model.findEntityDef(cmd.entityKey)
        entity.ensureAttributeDefExists(cmd.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateEntityDefAttributeDefHashtagAdd(
                modelId = model.id,
                entityKey = cmd.entityKey,
                attributeKey = cmd.attributeKey,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun updateEntityDefAttributeDefHashtagDelete(cmd: ModelCmd.UpdateEntityDefAttributeDefHashtagDelete) {
        val model = findModelByRef(cmd.modelRef)
        val entity = model.findEntityDef(cmd.entityKey)
        entity.ensureAttributeDefExists(cmd.attributeKey)
        storage.dispatch(
            ModelRepositoryCmd.UpdateEntityDefAttributeDefHashtagDelete(
                modelId = model.id,
                entityKey = cmd.entityKey,
                attributeKey = cmd.attributeKey,
                hashtag = cmd.hashtag
            )
        )
    }

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------


    private fun deleteRelationshipAttributeDef(cmd: ModelCmd.DeleteRelationshipAttributeDef) {
        val model = findModelByRef(cmd.modelRef)
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
        val model = findModelByRef(cmd.modelRef)
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
        val model = findModelByRef(cmd.modelRef)
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
        val model = findModelByRef(cmd.modelRef)
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
        val model = findModelByRef(cmd.modelRef)
        model.ensureRelationshipExists(cmd.relationshipKey)
        storage.dispatch(
            ModelRepositoryCmd.DeleteRelationshipDef(
                modelId = model.id,
                relationshipKey = cmd.relationshipKey
            )
        )
    }

    private fun updateRelationshipDef(cmd: ModelCmd.UpdateRelationshipDef) {
        val model = findModelByRef(cmd.modelRef)
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
        val model = findModelByRef(cmd.modelRef)
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
        val model = findModelByRef(cmd.modelRef)
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
        val model = findModelByRef(cmd.modelRef)
        val exists = model.findRelationshipDef(cmd.relationshipKey)
            .findAttributeDefOptional(cmd.attr.key)
        if (exists != null) {
            throw RelationshipDuplicateAttributeException(model.id, cmd.relationshipKey, cmd.attr.key)
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
        val model = findModelByRef(cmd.modelRef)
        if (model.findRelationshipDefOptional(cmd.initializer.key) != null)
            throw RelationshipDuplicateIdException(model.id, cmd.initializer.key)
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
    fun ensureModelExists(modelRef: ModelRef) {
        val exists = when(modelRef) {
            is ModelRef.ByKey -> storage.existsModelByKey(modelRef.key)
            is ModelRef.ById -> storage.existsModelById(modelRef.id)
        }
        if (!exists) throw ModelNotFoundException(modelRef)
    }



}
