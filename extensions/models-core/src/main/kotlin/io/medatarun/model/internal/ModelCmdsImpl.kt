package io.medatarun.model.internal

import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.ports.exposed.*
import io.medatarun.model.ports.needs.*

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
            is ModelCmd.CreateEntity -> createEntity(cmd)
            is ModelCmd.UpdateEntity -> updateEntity(cmd)
            is ModelCmd.UpdateEntityHashtagAdd -> updateEntityHashtagAdd(cmd)
            is ModelCmd.UpdateEntityHashtagDelete -> updateEntityHashtagDelete(cmd)
            is ModelCmd.DeleteEntity -> deleteEntity(cmd)
            is ModelCmd.CreateEntityAttribute -> createEntityAttribute(cmd)
            is ModelCmd.UpdateEntityAttribute -> updateEntityAttribute(cmd)
            is ModelCmd.UpdateEntityAttributeHashtagAdd -> updateEntityAttributeHashtagAdd(cmd)
            is ModelCmd.UpdateEntityAttributeHashtagDelete -> updateEntityAttributeHashtagDelete(cmd)
            is ModelCmd.DeleteEntityAttribute -> deleteEntityAttribute(cmd)
            is ModelCmd.CreateRelationship -> createRelationshipDef(cmd)
            is ModelCmd.CreateRelationshipAttribute -> createRelationshipAttributeDef(cmd)
            is ModelCmd.UpdateRelationship -> updateRelationship(cmd)
            is ModelCmd.UpdateRelationshipHashtagAdd -> updateRelationshipDefHashtagAdd(cmd)
            is ModelCmd.UpdateRelationshipHashtagDelete -> updateRelationshipDefHashtagDelete(cmd)
            is ModelCmd.DeleteRelationship -> deleteRelationship(cmd)
            is ModelCmd.UpdateRelationshipAttribute -> updateRelationshipAttribute(cmd)
            is ModelCmd.UpdateRelationshipAttributeHashtagAdd -> updateRelationshipAttributeHashtagAdd(cmd)
            is ModelCmd.UpdateRelationshipAttributeHashtagDelete -> updateRelationshipAttributeHashtagDelete(cmd)
            is ModelCmd.DeleteRelationshipAttribute -> deleteRelationshipAttribute(cmd)

        }
        return auditor.onCmdProcessed(cmd)
    }

    private fun findModelByIdOptional(id: ModelId): Model? {
        return storage.findModelByIdOptional(id)
    }

    private fun findModelByKeyOptional(key: ModelKey): Model? {
        return storage.findModelByKeyOptional(key)
    }

    private fun findModel(ref: ModelRef): Model {
        return when (ref) {
            is ModelRef.ByKey -> findModelByKeyOptional(ref.key)
            is ModelRef.ById -> findModelByIdOptional(ref.id)
        } ?: throw ModelNotFoundException(ref)
    }

    private fun findType(modelRef: ModelRef, typeRef: TypeRef): ModelType {
        val model = findModel(modelRef)
        val type = model.findTypeOptional(typeRef)
            ?: throw TypeNotFoundException(modelRef, typeRef)
        return type
    }

    private fun findEntity(modelRef: ModelRef, entityRef: EntityRef): EntityDef {
        val model = findModel(modelRef)
        val entity = model.findEntityOptional(entityRef)
            ?: throw EntityNotFoundException(modelRef, entityRef)
        return entity
    }

    private fun findEntityAttribute(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attrRef: EntityAttributeRef
    ): AttributeDef {
        val model = findModel(modelRef)
        val attr = model.findEntityAttributeOptional(entityRef, attrRef)
            ?: throw EntityAttributeNotFoundException(modelRef, entityRef, attrRef)
        return attr
    }

    private fun findEntityAttributeOptional(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attrRef: EntityAttributeRef
    ): AttributeDef? {
        val model = findModel(modelRef)
        val attr = model.findEntityAttributeOptional(entityRef, attrRef)
        return attr
    }

    private fun findRelationship(modelRef: ModelRef, relationshipRef: RelationshipRef): RelationshipDef {
        val model = findModel(modelRef)
        return model.findRelationshipOptional(relationshipRef)
            ?: throw RelationshipNotFoundException(modelRef, relationshipRef)
    }

    private fun findRelationshipRole(
        modelRef: ModelRef,
        relationshipRef: RelationshipRef,
        roleRef: RelationshipRoleRef
    ): RelationshipRole {
        val model = findModel(modelRef)
        return model.findRelationshipRoleOptional(relationshipRef, roleRef) ?: throw RelationshipRoleNotFoundException(
            modelRef,
            relationshipRef,
            roleRef
        )
    }

    private fun findRelationshipAttribute(
        modelRef: ModelRef,
        relationshipRef: RelationshipRef,
        attrRef: RelationshipAttributeRef
    ): AttributeDef {
        val model = findModel(modelRef)
        val attr = model.findRelationshipAttributeOptional(relationshipRef, attrRef)
            ?: throw RelationshipAttributeNotFoundException(modelRef, relationshipRef, attrRef)
        return attr
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
        storage.dispatch(ModelRepoCmd.CreateModel(model), cmd.repositoryRef)
    }

    private fun copyModel(cmd: ModelCmd.CopyModel) {
        val model = findModel(cmd.modelRef)
        val existing = storage.findModelByKeyOptional(cmd.modelNewKey)
        if (existing != null) throw ModelDuplicateIdException(cmd.modelNewKey)
        val next = ModelInMemory.of(model).copy(key = cmd.modelNewKey)
        storage.dispatch(ModelRepoCmd.CreateModel(next), cmd.repositoryRef)
    }

    private fun importModel(cmd: ModelCmd.ImportModel) {
        val existing = storage.findModelByKeyOptional(cmd.model.key)
        if (existing != null) throw ModelDuplicateIdException(cmd.model.key)
        storage.dispatch(ModelRepoCmd.CreateModel(cmd.model), cmd.repositoryRef)
    }


    private fun deleteModel(cmd: ModelCmd.DeleteModel) {
        val model = findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.DeleteModel(model.id))
    }

    private fun updateModelName(cmd: ModelCmd.UpdateModelName) {
        val model = findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelName(model.id, cmd.name))
    }

    private fun updateModelDescription(cmd: ModelCmd.UpdateModelDescription) {
        val model = findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelDescription(model.id, cmd.description))
    }

    private fun updateModelVersion(cmd: ModelCmd.UpdateModelVersion) {
        val model = findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelVersion(model.id, cmd.version))
    }

    private fun updateDocumentationHome(cmd: ModelCmd.UpdateModelDocumentationHome) {
        val model = findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelDocumentationHome(model.id, cmd.url))
    }

    private fun updateModelHashtagAdd(cmd: ModelCmd.UpdateModelHashtagAdd) {
        val model = findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelHashtagAdd(model.id, cmd.hashtag))
    }

    private fun updateModelHashtagDelete(cmd: ModelCmd.UpdateModelHashtagDelete) {
        val model = findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelHashtagDelete(model.id, cmd.hashtag))
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Types
    // -----------------------------------------------------------------------------------------------------------------

    private fun createType(cmd: ModelCmd.CreateType) {
        // Cannot create a type if another type already has the same key in the model
        val model = findModel(cmd.modelRef)
        val existing = model.findTypeOptional(cmd.initializer.id)
        if (existing != null) throw TypeCreateDuplicateException(model.key, cmd.initializer.id)
        storage.dispatch(ModelRepoCmd.CreateType(model.id, cmd.initializer))
    }

    private fun updateType(cmd: ModelCmd.UpdateType) {
        val model = findModel(cmd.modelRef)
        val type = model.findTypeOptional(cmd.typeRef) ?: throw TypeNotFoundException(cmd.modelRef, cmd.typeRef)
        storage.dispatch(ModelRepoCmd.UpdateType(model.id, type.id, cmd.cmd))
    }

    private fun deleteType(cmd: ModelCmd.DeleteType) {
        // Cannot delete type used in any entity
        val model = findModel(cmd.modelRef)
        val type = model.findTypeOptional(cmd.typeRef) ?: throw TypeNotFoundException(cmd.modelRef, cmd.typeRef)

        val used = model.entityDefs.any { entityDef -> entityDef.attributes.any { attr -> attr.type == type.key } }
        if (used) throw ModelTypeDeleteUsedException(type.key)

        storage.dispatch(ModelRepoCmd.DeleteType(model.id, type.id))
    }
    // -----------------------------------------------------------------------------------------------------------------
    // Entities
    // -----------------------------------------------------------------------------------------------------------------

    private fun updateEntity(cmd: ModelCmd.UpdateEntity) {
        val model = findModel(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val part = when (cmd.cmd) {
            is EntityDefUpdateCmd.Key -> {
                if (model.entityDefs.any { it.key == cmd.cmd.value && it.key != entity.key }) {
                    throw UpdateEntityDefIdDuplicateIdException(entity.key)
                }
                ModelRepoCmdEntityUpdate.Key(cmd.cmd.value)
            }

            is EntityDefUpdateCmd.Description -> ModelRepoCmdEntityUpdate.Description(cmd.cmd.value)
            is EntityDefUpdateCmd.Name -> ModelRepoCmdEntityUpdate.Name(cmd.cmd.value)
            is EntityDefUpdateCmd.DocumentationHome -> ModelRepoCmdEntityUpdate.DocumentationHome(cmd.cmd.value)
            is EntityDefUpdateCmd.IdentifierAttribute -> {
                val attrId = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.cmd.value).id
                ModelRepoCmdEntityUpdate.IdentifierAttribute(attrId)
            }
        }
        storage.dispatch(ModelRepoCmd.UpdateEntity(model.id, entity.id, part))
    }

    private fun updateEntityHashtagAdd(cmd: ModelCmd.UpdateEntityHashtagAdd) {
        val model = findModel(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        storage.dispatch(ModelRepoCmd.UpdateEntityHashtagAdd(model.id, entity.id, cmd.hashtag))
    }

    private fun updateEntityHashtagDelete(cmd: ModelCmd.UpdateEntityHashtagDelete) {
        val model = findModel(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        storage.dispatch(ModelRepoCmd.UpdateEntityHashtagDelete(model.id, entity.id, cmd.hashtag))
    }


    private fun createEntity(c: ModelCmd.CreateEntity) {
        val model = findModel(c.modelRef)
        val type = findType(c.modelRef, c.entityDefInitializer.identityAttribute.type)
        val identityAttribute = AttributeDefInMemory(
            id = AttributeId.generate(),
            key = c.entityDefInitializer.identityAttribute.attributeKey,
            name = c.entityDefInitializer.identityAttribute.name,
            description = c.entityDefInitializer.identityAttribute.description,
            type = type.key,
            optional = false, // because it's identity, can never be optional
            hashtags = emptyList()
        )
        val attributes = listOf(identityAttribute)
        storage.dispatch(
            ModelRepoCmd.CreateEntity(
                model.id,
                EntityDefInMemory(
                    id = EntityId.generate(),
                    key = c.entityDefInitializer.entityKey,
                    name = c.entityDefInitializer.name,
                    description = c.entityDefInitializer.description,
                    identifierAttributeId = identityAttribute.id,
                    origin = EntityOrigin.Manual,
                    documentationHome = c.entityDefInitializer.documentationHome,
                    hashtags = emptyList(),
                    attributes = attributes
                )
            )
        )
    }

    private fun deleteEntity(c: ModelCmd.DeleteEntity) {
        val model = findModel(c.modelRef)
        val entity = findEntity(c.modelRef, c.entityRef)
        storage.dispatch(ModelRepoCmd.DeleteEntity(model.id, entity.id))
    }

    private fun createEntityAttribute(c: ModelCmd.CreateEntityAttribute) {
        val model = findModel(c.modelRef)
        val entity = findEntity(c.modelRef, c.entityRef)
        val duplicate = findEntityAttributeOptional(c.modelRef, c.entityRef, EntityAttributeRef.ByKey(c.attributeInitializer.attributeKey))
        if (duplicate != null)
            throw CreateAttributeDefDuplicateIdException(entity.key, c.attributeInitializer.attributeKey)

        // Makes sure the type reference exists, even if now, the type is referenced by a key only
        val typeRef = c.attributeInitializer.type
        val type = findType(c.modelRef, typeRef)

        storage.dispatch(
            ModelRepoCmd.CreateEntityAttribute(
                modelId = model.id,
                entityId = entity.id,
                attributeDef = AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = c.attributeInitializer.attributeKey,
                    name = c.attributeInitializer.name,
                    description = c.attributeInitializer.description,
                    type = type.key,
                    optional = c.attributeInitializer.optional,
                    hashtags = emptyList()
                )
            )
        )
    }

    private fun deleteEntityAttribute(cmd: ModelCmd.DeleteEntityAttribute) {
        val model = findModel(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attr = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (entity.identifierAttributeKey == attr.key)
            throw DeleteAttributeIdentifierException(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.DeleteEntityAttribute(
                modelId = model.id,
                entityId = entity.id,
                attributeId = attr.id
            )
        )
    }

    private fun updateEntityAttribute(cmd: ModelCmd.UpdateEntityAttribute) {
        val model = findModel(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attribute = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)


        // TODO how do we ensure transactions here ?


        val part: ModelRepoCmdAttributeUpdate = when (cmd.cmd) {
            is AttributeDefUpdateCmd.Name -> ModelRepoCmdAttributeUpdate.Name(cmd.cmd.value)
            is AttributeDefUpdateCmd.Description -> ModelRepoCmdAttributeUpdate.Description(cmd.cmd.value)
            is AttributeDefUpdateCmd.Optional -> ModelRepoCmdAttributeUpdate.Optional(cmd.cmd.value)
            is AttributeDefUpdateCmd.Key -> {
                // We cannot have two attributes with the same key
                if (entity.attributes.any { it.key == cmd.cmd.value && it.key != attribute.key }) {
                    throw UpdateAttributeDuplicateKeyException(cmd.entityRef, cmd.attributeRef, cmd.cmd.value)
                }
                ModelRepoCmdAttributeUpdate.Key(cmd.cmd.value)
            }

            is AttributeDefUpdateCmd.Type -> {
                // Attribute type shall exist when updating types
                val typeRef = cmd.cmd.value
                val type = findType(cmd.modelRef, typeRef)
                ModelRepoCmdAttributeUpdate.Type(type.id)
            }


        }
        // Apply changes on attribute
        storage.dispatch(
            ModelRepoCmd.UpdateEntityAttribute(
                modelId = model.id,
                entityId = entity.id,
                attributeId = attribute.id,
                cmd = part
            )
        )
    }

    private fun updateEntityAttributeHashtagAdd(cmd: ModelCmd.UpdateEntityAttributeHashtagAdd) {
        val model = findModel(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attribute = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.UpdateEntityAttributeHashtagAdd(
                modelId = model.id,
                entityId = entity.id,
                attributeId = attribute.id,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun updateEntityAttributeHashtagDelete(cmd: ModelCmd.UpdateEntityAttributeHashtagDelete) {
        val model = findModel(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attribute = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.UpdateEntityAttributeHashtagDelete(
                modelId = model.id,
                entityId = entity.id,
                attributeId = attribute.id,
                hashtag = cmd.hashtag
            )
        )
    }

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------


    private fun deleteRelationshipAttribute(cmd: ModelCmd.DeleteRelationshipAttribute) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.DeleteRelationshipAttribute(
                modelId = model.id,
                relationshipId = rel.id,
                attributeId = attr.id
            )
        )
    }

    private fun updateRelationshipAttribute(cmd: ModelCmd.UpdateRelationshipAttribute) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        val part: ModelRepoCmdAttributeUpdate = when(cmd.cmd) {
            is AttributeDefUpdateCmd.Name -> ModelRepoCmdAttributeUpdate.Name(cmd.cmd.value)
            is AttributeDefUpdateCmd.Description -> ModelRepoCmdAttributeUpdate.Description(cmd.cmd.value)
            is AttributeDefUpdateCmd.Key -> ModelRepoCmdAttributeUpdate.Key(cmd.cmd.value)
            is AttributeDefUpdateCmd.Optional -> ModelRepoCmdAttributeUpdate.Optional(cmd.cmd.value)
            is AttributeDefUpdateCmd.Type -> {
                val type = findType(cmd.modelRef, cmd.cmd.value)
                ModelRepoCmdAttributeUpdate.Type(type.id)
            }
        }
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttribute(
                modelId = model.id,
                relationshipId = rel.id,
                attributeId = attr.id,
                cmd = part
            )
        )
    }

    private fun updateRelationshipAttributeHashtagAdd(cmd: ModelCmd.UpdateRelationshipAttributeHashtagAdd) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeHashtagAdd(
                modelId = model.id,
                relationshipId = rel.id,
                attributeId = attr.id,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun updateRelationshipAttributeHashtagDelete(cmd: ModelCmd.UpdateRelationshipAttributeHashtagDelete) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeHashtagDelete(
                modelId = model.id,
                relationshipId = rel.id,
                attributeId = attr.id,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun deleteRelationship(cmd: ModelCmd.DeleteRelationship) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        storage.dispatch(
            ModelRepoCmd.DeleteRelationship(
                modelId = model.id,
                relationshipId = rel.id,
            )
        )
    }

    private fun updateRelationship(cmd: ModelCmd.UpdateRelationship) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val part = when (cmd.cmd) {
            is RelationshipDefUpdateCmd.Key -> ModelRepoCmdRelationshipDefUpdate.Key(cmd.cmd.value)
            is RelationshipDefUpdateCmd.Name -> ModelRepoCmdRelationshipDefUpdate.Name(cmd.cmd.value)
            is RelationshipDefUpdateCmd.Description -> ModelRepoCmdRelationshipDefUpdate.Description(cmd.cmd.value)
            is RelationshipDefUpdateCmd.RoleCardinality -> ModelRepoCmdRelationshipDefUpdate.RoleCardinality(
                findRelationshipRole(cmd.modelRef, cmd.relationshipRef, cmd.cmd.relationshipRoleRef).id,
                cmd.cmd.value
            )

            is RelationshipDefUpdateCmd.RoleEntity -> ModelRepoCmdRelationshipDefUpdate.RoleEntity(
                findRelationshipRole(cmd.modelRef, cmd.relationshipRef, cmd.cmd.relationshipRoleRef).id,
                findEntity(cmd.modelRef, cmd.cmd.value).id
            )

            is RelationshipDefUpdateCmd.RoleKey -> ModelRepoCmdRelationshipDefUpdate.RoleKey(
                findRelationshipRole(cmd.modelRef, cmd.relationshipRef, cmd.cmd.relationshipRoleRef).id,
                cmd.cmd.value
            )

            is RelationshipDefUpdateCmd.RoleName -> ModelRepoCmdRelationshipDefUpdate.RoleName(
                findRelationshipRole(cmd.modelRef, cmd.relationshipRef, cmd.cmd.relationshipRoleRef).id,
                cmd.cmd.value
            )
        }
        storage.dispatch(
            ModelRepoCmd.UpdateRelationship(
                modelId = model.id,
                relationshipId = rel.id,
                cmd = part
            )
        )
    }

    private fun updateRelationshipDefHashtagAdd(cmd: ModelCmd.UpdateRelationshipHashtagAdd) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipHashtagAdd(
                modelId = model.id,
                relationshipId = rel.id,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun updateRelationshipDefHashtagDelete(cmd: ModelCmd.UpdateRelationshipHashtagDelete) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipHashtagDelete(
                modelId = model.id,
                relationshipId = rel.id,
                hashtag = cmd.hashtag
            )
        )
    }

    private fun createRelationshipAttributeDef(cmd: ModelCmd.CreateRelationshipAttribute) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val exists = model.findRelationshipAttributeOptional(cmd.relationshipRef, cmd.attr.attributeKey)
        if (exists != null) {
            throw RelationshipAttributeCreateDuplicateKeyException(cmd.modelRef, cmd.relationshipRef, cmd.attr.attributeKey)
        }
        val type = findType(cmd.modelRef, cmd.attr.type)
        storage.dispatch(
            ModelRepoCmd.CreateRelationshipAttribute(
                modelId = model.id,
                attr = AttributeDefInMemory(
                    id = AttributeId.generate(),
                    key = cmd.attr.attributeKey,
                    name = cmd.attr.name,
                    description = cmd.attr.description,
                    type = type.key,
                    optional = cmd.attr.optional,
                    hashtags = emptyList()
                ),
                relationshipId = rel.id
            )
        )
    }


    private fun createRelationshipDef(cmd: ModelCmd.CreateRelationship) {
        val model = findModel(cmd.modelRef)
        if (model.findRelationshipOptional(cmd.initializer.key) != null)
            throw RelationshipDuplicateIdException(model.id, cmd.initializer.key)
        val duplicateRoleIds =
            cmd.initializer.roles.groupBy { it.key }.mapValues { it.value.size }.filter { it.value > 1 }
        if (duplicateRoleIds.isNotEmpty()) {
            throw RelationshipDuplicateRoleIdException(duplicateRoleIds.keys)
        }
        storage.dispatch(
            ModelRepoCmd.CreateRelationship(
                modelId = model.id,
                initializer = RelationshipDefInMemory(
                    id = RelationshipId.generate(),
                    name = cmd.initializer.name,
                    description = cmd.initializer.description,
                    key = cmd.initializer.key,
                    roles = cmd.initializer.roles.map {
                        val entity = model.findEntityOptional(it.entityRef) ?: throw EntityNotFoundException(
                            cmd.modelRef,
                            it.entityRef
                        )
                        RelationshipRoleInMemory(
                            id = RelationshipRoleId.generate(),
                            key = it.key,
                            entityKey = entity.key,
                            name = it.name,
                            cardinality = it.cardinality
                        )
                    },
                    hashtags = emptyList(),
                    attributes = emptyList(),
                )
            )
        )
    }

    fun ensureModelExists(modelRef: ModelRef) {
        val exists = when (modelRef) {
            is ModelRef.ByKey -> storage.existsModelByKey(modelRef.key)
            is ModelRef.ById -> storage.existsModelById(modelRef.id)
        }
        if (!exists) throw ModelNotFoundException(modelRef)
    }


}
