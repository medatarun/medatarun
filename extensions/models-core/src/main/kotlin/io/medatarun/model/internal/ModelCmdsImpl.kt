package io.medatarun.model.internal

import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.ports.exposed.*
import io.medatarun.model.ports.needs.*
import io.medatarun.tags.core.domain.TagRef

class ModelCmdsImpl(
    val storage: ModelStorages,
    val auditor: ModelAuditor,
    val tagResolver: ModelTagResolver
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
            is ModelCmd.UpdateModelTagAdd -> updateModelTagAdd(cmd)
            is ModelCmd.UpdateModelTagDelete -> updateModelTagDelete(cmd)
            is ModelCmd.DeleteModel -> deleteModel(cmd)
            is ModelCmd.CreateType -> createType(cmd)
            is ModelCmd.UpdateType -> updateType(cmd)
            is ModelCmd.DeleteType -> deleteType(cmd)
            is ModelCmd.CreateEntity -> createEntity(cmd)
            is ModelCmd.UpdateEntity -> updateEntity(cmd)
            is ModelCmd.UpdateEntityTagAdd -> updateEntityTagAdd(cmd)
            is ModelCmd.UpdateEntityTagDelete -> updateEntityTagDelete(cmd)
            is ModelCmd.DeleteEntity -> deleteEntity(cmd)
            is ModelCmd.CreateEntityAttribute -> createEntityAttribute(cmd)
            is ModelCmd.UpdateEntityAttribute -> updateEntityAttribute(cmd)
            is ModelCmd.UpdateEntityAttributeTagAdd -> updateEntityAttributeTagAdd(cmd)
            is ModelCmd.UpdateEntityAttributeTagDelete -> updateEntityAttributeTagDelete(cmd)
            is ModelCmd.DeleteEntityAttribute -> deleteEntityAttribute(cmd)
            is ModelCmd.CreateRelationship -> createRelationship(cmd)
            is ModelCmd.CreateRelationshipAttribute -> createRelationshipAttribute(cmd)
            is ModelCmd.UpdateRelationship -> updateRelationship(cmd)
            is ModelCmd.UpdateRelationshipTagAdd -> updateRelationshipTagAdd(cmd)
            is ModelCmd.UpdateRelationshipTagDelete -> updateRelationshipTagDelete(cmd)
            is ModelCmd.DeleteRelationship -> deleteRelationship(cmd)
            is ModelCmd.UpdateRelationshipAttribute -> updateRelationshipAttribute(cmd)
            is ModelCmd.UpdateRelationshipAttributeTagAdd -> updateRelationshipAttributeTagAdd(cmd)
            is ModelCmd.UpdateRelationshipAttributeTagDelete -> updateRelationshipAttributeTagDelete(cmd)
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

    private fun findEntity(modelRef: ModelRef, entityRef: EntityRef): Entity {
        val model = findModel(modelRef)
        val entity = model.findEntityOptional(entityRef)
            ?: throw EntityNotFoundException(modelRef, entityRef)
        return entity
    }

    private fun findEntityAttribute(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attrRef: EntityAttributeRef
    ): Attribute {
        val model = findModel(modelRef)
        val attr = model.findEntityAttributeOptional(entityRef, attrRef)
            ?: throw EntityAttributeNotFoundException(modelRef, entityRef, attrRef)
        return attr
    }

    private fun findEntityAttributeOptional(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attrRef: EntityAttributeRef
    ): Attribute? {
        val model = findModel(modelRef)
        val attr = model.findEntityAttributeOptional(entityRef, attrRef)
        return attr
    }

    private fun findRelationship(modelRef: ModelRef, relationshipRef: RelationshipRef): Relationship {
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
    ): Attribute {
        val model = findModel(modelRef)
        val attr = model.findRelationshipAttributeOptional(relationshipRef, attrRef)
            ?: throw RelationshipAttributeNotFoundException(modelRef, relationshipRef, attrRef)
        return attr
    }

    private fun findTagId(tagRef: TagRef) = tagResolver.resolveTagId(tagRef)


    private fun createModel(cmd: ModelCmd.CreateModel) {
        val model = ModelInMemory(
            id = ModelId.generate(),
            key = cmd.modelKey,
            name = cmd.name,
            description = cmd.description,
            version = cmd.version,
            origin = ModelOrigin.Manual,
            types = emptyList(),
            entities = emptyList(),
            relationships = emptyList(),
            documentationHome = null,
            tags = emptyList(),
        )
        storage.dispatch(ModelRepoCmd.CreateModel(model), cmd.repositoryRef)
    }


    private fun copyModel(cmd: ModelCmd.CopyModel) {
        val model = findModel(cmd.modelRef)
        val existing = storage.findModelByKeyOptional(cmd.modelNewKey)
        if (existing != null) throw ModelDuplicateIdException(cmd.modelNewKey)
        val next = ModelCmdCopyImpl().copy(model, cmd.modelNewKey)
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

    private fun updateModelTagAdd(cmd: ModelCmd.UpdateModelTagAdd) {
        val model = findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelTagAdd(model.id, findTagId(cmd.tagRef)))
    }

    private fun updateModelTagDelete(cmd: ModelCmd.UpdateModelTagDelete) {
        val model = findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelTagDelete(model.id, findTagId(cmd.tagRef)))
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
        val subcommand = when (cmd.cmd) {
            is ModelTypeUpdateCmd.Name -> {
                if (model.name == cmd.cmd.value) return
                cmd.cmd
            }
            is ModelTypeUpdateCmd.Description -> {
                if (model.description == cmd.cmd.value) return
                cmd.cmd
            }
            is ModelTypeUpdateCmd.Key -> {
                if (type.key == cmd.cmd.value) return
                val found = model.findTypeOptional(cmd.cmd.value)
                if (found != null) throw TypeUpdateDuplicateKeyException(cmd.cmd.value)
                cmd.cmd
            }
        }

        storage.dispatch(ModelRepoCmd.UpdateType(model.id, type.id, subcommand))

    }

    private fun deleteType(cmd: ModelCmd.DeleteType) {
        // Cannot delete type used in any entity
        val model = findModel(cmd.modelRef)
        val type = model.findTypeOptional(cmd.typeRef) ?: throw TypeNotFoundException(cmd.modelRef, cmd.typeRef)

        val used = model.entities.any { entity -> entity.attributes.any { attr -> attr.typeId == type.id } }
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
            is EntityUpdateCmd.Key -> {
                if (model.entities.any { it.key == cmd.cmd.value && it.key != entity.key }) {
                    throw EntityUpdateIdDuplicateIdException(entity.key)
                }
                ModelRepoCmdEntityUpdate.Key(cmd.cmd.value)
            }

            is EntityUpdateCmd.Description -> ModelRepoCmdEntityUpdate.Description(cmd.cmd.value)
            is EntityUpdateCmd.Name -> ModelRepoCmdEntityUpdate.Name(cmd.cmd.value)
            is EntityUpdateCmd.DocumentationHome -> ModelRepoCmdEntityUpdate.DocumentationHome(cmd.cmd.value)
            is EntityUpdateCmd.IdentifierAttribute -> {
                val attrId = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.cmd.value).id
                ModelRepoCmdEntityUpdate.IdentifierAttribute(attrId)
            }
        }
        storage.dispatch(ModelRepoCmd.UpdateEntity(model.id, entity.id, part))
    }

    private fun updateEntityTagAdd(cmd: ModelCmd.UpdateEntityTagAdd) {
        val model = findModel(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        storage.dispatch(ModelRepoCmd.UpdateEntityTagAdd(model.id, entity.id, findTagId(cmd.tagRef)))
    }

    private fun updateEntityTagDelete(cmd: ModelCmd.UpdateEntityTagDelete) {
        val model = findModel(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        storage.dispatch(ModelRepoCmd.UpdateEntityTagDelete(model.id, entity.id, findTagId(cmd.tagRef)))
    }


    private fun createEntity(c: ModelCmd.CreateEntity) {
        val model = findModel(c.modelRef)
        val type = findType(c.modelRef, c.entityInitializer.identityAttribute.type)
        val identityAttribute = AttributeInMemory(
            id = AttributeId.generate(),
            key = c.entityInitializer.identityAttribute.attributeKey,
            name = c.entityInitializer.identityAttribute.name,
            description = c.entityInitializer.identityAttribute.description,
            typeId = type.id,
            optional = false, // because it's identity, can never be optional
            tags = emptyList(),
        )
        val attributes = listOf(identityAttribute)
        storage.dispatch(
            ModelRepoCmd.CreateEntity(
                model.id,
                EntityInMemory(
                    id = EntityId.generate(),
                    key = c.entityInitializer.entityKey,
                    name = c.entityInitializer.name,
                    description = c.entityInitializer.description,
                    identifierAttributeId = identityAttribute.id,
                    origin = EntityOrigin.Manual,
                    documentationHome = c.entityInitializer.documentationHome,
                    tags = emptyList(),
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
        val duplicate = findEntityAttributeOptional(
            c.modelRef,
            c.entityRef,
            EntityAttributeRef.ByKey(c.attributeInitializer.attributeKey)
        )
        if (duplicate != null)
            throw CreateAttributeDuplicateIdException(entity.key, c.attributeInitializer.attributeKey)

        // Makes sure the type reference exists, even if now, the type is referenced by a key only
        val typeRef = c.attributeInitializer.type
        val type = findType(c.modelRef, typeRef)

        storage.dispatch(
            ModelRepoCmd.CreateEntityAttribute(
                modelId = model.id,
                entityId = entity.id,
                attribute = AttributeInMemory(
                    id = AttributeId.generate(),
                    key = c.attributeInitializer.attributeKey,
                    name = c.attributeInitializer.name,
                    description = c.attributeInitializer.description,
                    typeId = type.id,
                    optional = c.attributeInitializer.optional,
                    tags = emptyList(),
                )
            )
        )
    }

    private fun deleteEntityAttribute(cmd: ModelCmd.DeleteEntityAttribute) {
        val model = findModel(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attr = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (entity.identifierAttributeId == attr.id)
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
            is AttributeUpdateCmd.Name -> ModelRepoCmdAttributeUpdate.Name(cmd.cmd.value)
            is AttributeUpdateCmd.Description -> ModelRepoCmdAttributeUpdate.Description(cmd.cmd.value)
            is AttributeUpdateCmd.Optional -> ModelRepoCmdAttributeUpdate.Optional(cmd.cmd.value)
            is AttributeUpdateCmd.Key -> {
                // We cannot have two attributes with the same key
                if (entity.attributes.any { it.key == cmd.cmd.value && it.key != attribute.key }) {
                    throw UpdateAttributeDuplicateKeyException(cmd.entityRef, cmd.attributeRef, cmd.cmd.value)
                }
                ModelRepoCmdAttributeUpdate.Key(cmd.cmd.value)
            }

            is AttributeUpdateCmd.Type -> {
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

    private fun updateEntityAttributeTagAdd(cmd: ModelCmd.UpdateEntityAttributeTagAdd) {
        val model = findModel(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attribute = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.UpdateEntityAttributeTagAdd(
                modelId = model.id,
                entityId = entity.id,
                attributeId = attribute.id,
                tagId = findTagId(cmd.tagRef)
            )
        )
    }

    private fun updateEntityAttributeTagDelete(cmd: ModelCmd.UpdateEntityAttributeTagDelete) {
        val model = findModel(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attribute = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.UpdateEntityAttributeTagDelete(
                modelId = model.id,
                entityId = entity.id,
                attributeId = attribute.id,
                tagId = findTagId(cmd.tagRef)
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
        val part: ModelRepoCmdAttributeUpdate = when (cmd.cmd) {
            is AttributeUpdateCmd.Name -> ModelRepoCmdAttributeUpdate.Name(cmd.cmd.value)
            is AttributeUpdateCmd.Description -> ModelRepoCmdAttributeUpdate.Description(cmd.cmd.value)
            is AttributeUpdateCmd.Key -> {
                // We cannot have two attributes with the same key
                if (rel.attributes.any { it.key == cmd.cmd.value && it.key != attr.key }) {
                    throw RelationshipAttributeUpdateDuplicateKeyException(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef, cmd.cmd.value)
                }
                ModelRepoCmdAttributeUpdate.Key(cmd.cmd.value)
            }
            is AttributeUpdateCmd.Optional -> ModelRepoCmdAttributeUpdate.Optional(cmd.cmd.value)
            is AttributeUpdateCmd.Type -> {
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

    private fun updateRelationshipAttributeTagAdd(cmd: ModelCmd.UpdateRelationshipAttributeTagAdd) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeTagAdd(
                modelId = model.id,
                relationshipId = rel.id,
                attributeId = attr.id,
                tagId = findTagId(cmd.tagRef)
            )
        )
    }

    private fun updateRelationshipAttributeTagDelete(cmd: ModelCmd.UpdateRelationshipAttributeTagDelete) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeTagDelete(
                modelId = model.id,
                relationshipId = rel.id,
                attributeId = attr.id,
                tagId = findTagId(cmd.tagRef)
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
            is RelationshipUpdateCmd.Key -> ModelRepoCmdRelationshipUpdate.Key(cmd.cmd.value)
            is RelationshipUpdateCmd.Name -> ModelRepoCmdRelationshipUpdate.Name(cmd.cmd.value)
            is RelationshipUpdateCmd.Description -> ModelRepoCmdRelationshipUpdate.Description(cmd.cmd.value)
            is RelationshipUpdateCmd.RoleCardinality -> ModelRepoCmdRelationshipUpdate.RoleCardinality(
                findRelationshipRole(cmd.modelRef, cmd.relationshipRef, cmd.cmd.relationshipRoleRef).id,
                cmd.cmd.value
            )

            is RelationshipUpdateCmd.RoleEntity -> ModelRepoCmdRelationshipUpdate.RoleEntity(
                findRelationshipRole(cmd.modelRef, cmd.relationshipRef, cmd.cmd.relationshipRoleRef).id,
                findEntity(cmd.modelRef, cmd.cmd.value).id
            )

            is RelationshipUpdateCmd.RoleKey -> ModelRepoCmdRelationshipUpdate.RoleKey(
                findRelationshipRole(cmd.modelRef, cmd.relationshipRef, cmd.cmd.relationshipRoleRef).id,
                cmd.cmd.value
            )

            is RelationshipUpdateCmd.RoleName -> ModelRepoCmdRelationshipUpdate.RoleName(
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

    private fun updateRelationshipTagAdd(cmd: ModelCmd.UpdateRelationshipTagAdd) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipTagAdd(
                modelId = model.id,
                relationshipId = rel.id,
                tagId = findTagId(cmd.tagRef)
            )
        )
    }

    private fun updateRelationshipTagDelete(cmd: ModelCmd.UpdateRelationshipTagDelete) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipTagDelete(
                modelId = model.id,
                relationshipId = rel.id,
                tagId = findTagId(cmd.tagRef)
            )
        )
    }

    private fun createRelationshipAttribute(cmd: ModelCmd.CreateRelationshipAttribute) {
        val model = findModel(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val exists = model.findRelationshipAttributeOptional(cmd.relationshipRef, cmd.attr.attributeKey)
        if (exists != null) {
            throw RelationshipAttributeCreateDuplicateKeyException(
                cmd.modelRef,
                cmd.relationshipRef,
                cmd.attr.attributeKey
            )
        }
        val type = findType(cmd.modelRef, cmd.attr.type)
        storage.dispatch(
            ModelRepoCmd.CreateRelationshipAttribute(
                modelId = model.id,
                attr = AttributeInMemory(
                    id = AttributeId.generate(),
                    key = cmd.attr.attributeKey,
                    name = cmd.attr.name,
                    description = cmd.attr.description,
                    typeId = type.id,
                    optional = cmd.attr.optional,
                    tags = emptyList(),
                ),
                relationshipId = rel.id
            )
        )
    }


    private fun createRelationship(cmd: ModelCmd.CreateRelationship) {
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
                initializer = RelationshipInMemory(
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
                            entityId = entity.id,
                            name = it.name,
                            cardinality = it.cardinality
                        )
                    },
                    tags = emptyList(),
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
