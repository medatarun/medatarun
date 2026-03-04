package io.medatarun.model.internal

import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmdOnModel
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.needs.ModelRepoCmd
import io.medatarun.model.ports.needs.ModelStorage
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.platform.db.DbTransactionManager

class ModelCmdsImpl(
    private val storage: ModelStorage,
    private val modelValidation: ModelValidation,
    private val auditor: ModelAuditor,
    private val tagResolver: ModelTagResolver,
    private val txManager: DbTransactionManager
) : ModelCmds {

    override fun dispatch(cmd: ModelCmd) {
        return txManager.runInTransaction {
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
                is ModelCmd.UpdateTypeKey -> updateTypeKey(cmd)
                is ModelCmd.UpdateTypeName -> updateTypeName(cmd)
                is ModelCmd.UpdateTypeDescription -> updateTypeDescription(cmd)
                is ModelCmd.DeleteType -> deleteType(cmd)
                is ModelCmd.CreateEntity -> createEntity(cmd)
                is ModelCmd.UpdateEntityKey -> updateEntityKey(cmd)
                is ModelCmd.UpdateEntityName -> updateEntityName(cmd)
                is ModelCmd.UpdateEntityDescription -> updateEntityDescription(cmd)
                is ModelCmd.UpdateEntityIdentifierAttribute -> updateEntityIdentifierAttribute(cmd)
                is ModelCmd.UpdateEntityDocumentationHome -> updateEntityDocumentationHome(cmd)
                is ModelCmd.UpdateEntityTagAdd -> updateEntityTagAdd(cmd)
                is ModelCmd.UpdateEntityTagDelete -> updateEntityTagDelete(cmd)
                is ModelCmd.DeleteEntity -> deleteEntity(cmd)
                is ModelCmd.CreateEntityAttribute -> createEntityAttribute(cmd)
                is ModelCmd.UpdateEntityAttributeKey -> updateEntityAttributeKey(cmd)
                is ModelCmd.UpdateEntityAttributeName -> updateEntityAttributeName(cmd)
                is ModelCmd.UpdateEntityAttributeDescription -> updateEntityAttributeDescription(cmd)
                is ModelCmd.UpdateEntityAttributeType -> updateEntityAttributeType(cmd)
                is ModelCmd.UpdateEntityAttributeOptional -> updateEntityAttributeOptional(cmd)
                is ModelCmd.UpdateEntityAttributeTagAdd -> updateEntityAttributeTagAdd(cmd)
                is ModelCmd.UpdateEntityAttributeTagDelete -> updateEntityAttributeTagDelete(cmd)
                is ModelCmd.DeleteEntityAttribute -> deleteEntityAttribute(cmd)
                is ModelCmd.CreateRelationship -> createRelationship(cmd)
                is ModelCmd.CreateRelationshipAttribute -> createRelationshipAttribute(cmd)
                is ModelCmd.UpdateRelationshipKey -> updateRelationshipKey(cmd)
                is ModelCmd.UpdateRelationshipName -> updateRelationshipName(cmd)
                is ModelCmd.UpdateRelationshipDescription -> updateRelationshipDescription(cmd)
                is ModelCmd.UpdateRelationshipRoleKey -> updateRelationshipRoleKey(cmd)
                is ModelCmd.UpdateRelationshipRoleName -> updateRelationshipRoleName(cmd)
                is ModelCmd.UpdateRelationshipRoleEntity -> updateRelationshipRoleEntity(cmd)
                is ModelCmd.UpdateRelationshipRoleCardinality -> updateRelationshipRoleCardinality(cmd)
                is ModelCmd.UpdateRelationshipTagAdd -> updateRelationshipTagAdd(cmd)
                is ModelCmd.UpdateRelationshipTagDelete -> updateRelationshipTagDelete(cmd)
                is ModelCmd.DeleteRelationship -> deleteRelationship(cmd)
                is ModelCmd.UpdateRelationshipAttributeKey -> updateRelationshipAttributeKey(cmd)
                is ModelCmd.UpdateRelationshipAttributeName -> updateRelationshipAttributeName(cmd)
                is ModelCmd.UpdateRelationshipAttributeDescription -> updateRelationshipAttributeDescription(cmd)
                is ModelCmd.UpdateRelationshipAttributeType -> updateRelationshipAttributeType(cmd)
                is ModelCmd.UpdateRelationshipAttributeOptional -> updateRelationshipAttributeOptional(cmd)
                is ModelCmd.UpdateRelationshipAttributeTagAdd -> updateRelationshipAttributeTagAdd(cmd)
                is ModelCmd.UpdateRelationshipAttributeTagDelete -> updateRelationshipAttributeTagDelete(cmd)
                is ModelCmd.DeleteRelationshipAttribute -> deleteRelationshipAttribute(cmd)
            }
            auditor.onCmdProcessed(cmd)
        }
    }

    private fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate? {
        return storage.findModelAggregateByIdOptional(id)
    }

    private fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate? {
        return storage.findModelAggregateByKeyOptional(key)
    }

    private fun findModelAggregate(ref: ModelRef): ModelAggregate {
        return when (ref) {
            is ModelRef.ByKey -> findModelAggregateByKeyOptional(ref.key)
            is ModelRef.ById -> findModelAggregateByIdOptional(ref.id)
        } ?: throw ModelNotFoundException(ref)
    }

    private fun findType(modelRef: ModelRef, typeRef: TypeRef): ModelType {
        val model = findModelAggregate(modelRef)
        val type = model.findTypeOptional(typeRef)
            ?: throw TypeNotFoundException(modelRef, typeRef)
        return type
    }

    private fun findEntity(modelRef: ModelRef, entityRef: EntityRef): Entity {
        val model = findModelAggregate(modelRef)
        val entity = model.findEntityOptional(entityRef)
            ?: throw EntityNotFoundException(modelRef, entityRef)
        return entity
    }

    private fun findEntityAttribute(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attrRef: EntityAttributeRef
    ): Attribute {
        val model = findModelAggregate(modelRef)
        val attr = model.findEntityAttributeOptional(entityRef, attrRef)
            ?: throw EntityAttributeNotFoundException(modelRef, entityRef, attrRef)
        return attr
    }

    private fun findEntityAttributeOptional(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attrRef: EntityAttributeRef
    ): Attribute? {
        val model = findModelAggregate(modelRef)
        val attr = model.findEntityAttributeOptional(entityRef, attrRef)
        return attr
    }

    private fun findRelationship(modelRef: ModelRef, relationshipRef: RelationshipRef): Relationship {
        val model = findModelAggregate(modelRef)
        return model.findRelationshipOptional(relationshipRef)
            ?: throw RelationshipNotFoundException(modelRef, relationshipRef)
    }

    private fun findRelationshipRole(
        modelRef: ModelRef,
        relationshipRef: RelationshipRef,
        roleRef: RelationshipRoleRef
    ): RelationshipRole {
        val model = findModelAggregate(modelRef)
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
        val model = findModelAggregate(modelRef)
        val attr = model.findRelationshipAttributeOptional(relationshipRef, attrRef)
            ?: throw RelationshipAttributeNotFoundException(modelRef, relationshipRef, attrRef)
        return attr
    }


    private fun createModel(cmd: ModelCmd.CreateModel) {
        val model = ModelAggregateInMemory(
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
        storage.dispatch(ModelRepoCmd.CreateModel(model))
    }


    private fun copyModel(cmd: ModelCmd.CopyModel) {
        val model = findModelAggregate(cmd.modelRef)
        if (storage.existsModelByKey(cmd.modelNewKey)) throw ModelDuplicateIdException(cmd.modelNewKey)
        val next = ModelCmdCopyImpl().copy(model, cmd.modelNewKey)
        storage.dispatch(ModelRepoCmd.CreateModel(next))
    }

    private fun importModel(cmd: ModelCmd.ImportModel) {
        if (storage.existsModelByKey(cmd.model.key)) throw ModelDuplicateIdException(cmd.model.key)
        ensureImportedModelIsValid(cmd.model)
        storage.dispatch(ModelRepoCmd.CreateModel(cmd.model))
    }


    private fun deleteModel(cmd: ModelCmd.DeleteModel) {
        val model = findModelAggregate(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.DeleteModel(model.id))
    }

    private fun updateModelName(cmd: ModelCmd.UpdateModelName) {
        val model = findModelAggregate(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelName(model.id, cmd.name))
    }

    private fun updateModelDescription(cmd: ModelCmd.UpdateModelDescription) {
        val model = findModelAggregate(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelDescription(model.id, cmd.description))
    }

    private fun updateModelVersion(cmd: ModelCmd.UpdateModelVersion) {
        val model = findModelAggregate(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelVersion(model.id, cmd.version))
    }

    private fun updateDocumentationHome(cmd: ModelCmd.UpdateModelDocumentationHome) {
        val model = findModelAggregate(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelDocumentationHome(model.id, cmd.url))
    }

    private fun updateModelTagAdd(cmd: ModelCmd.UpdateModelTagAdd) {
        val model = findModelAggregate(cmd.modelRef)
        storage.dispatch(
            ModelRepoCmd.UpdateModelTagAdd(
                model.id,
                tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateModelTagDelete(cmd: ModelCmd.UpdateModelTagDelete) {
        val model = findModelAggregate(cmd.modelRef)
        storage.dispatch(
            ModelRepoCmd.UpdateModelTagDelete(
                model.id,
                tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Types
    // -----------------------------------------------------------------------------------------------------------------

    private fun createType(cmd: ModelCmd.CreateType) {
        // Cannot create a type if another type already has the same key in the model
        val model = findModelAggregate(cmd.modelRef)
        val existing = model.findTypeOptional(cmd.initializer.id)
        if (existing != null) throw TypeCreateDuplicateException(model.key, cmd.initializer.id)
        storage.dispatch(ModelRepoCmd.CreateType(model.id, cmd.initializer))
    }

    private fun updateTypeKey(cmd: ModelCmd.UpdateTypeKey) {
        val model = findModelAggregate(cmd.modelRef)
        val type = model.findTypeOptional(cmd.typeRef) ?: throw TypeNotFoundException(cmd.modelRef, cmd.typeRef)
        if (type.key == cmd.value) return
        val found = model.findTypeOptional(cmd.value)
        if (found != null) throw TypeUpdateDuplicateKeyException(cmd.value)
        storage.dispatch(ModelRepoCmd.UpdateTypeKey(model.id, type.id, cmd.value))
    }

    private fun updateTypeName(cmd: ModelCmd.UpdateTypeName) {
        val model = findModelAggregate(cmd.modelRef)
        val type = model.findTypeOptional(cmd.typeRef) ?: throw TypeNotFoundException(cmd.modelRef, cmd.typeRef)
        if (type.name == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateTypeName(model.id, type.id, cmd.value))
    }

    private fun updateTypeDescription(cmd: ModelCmd.UpdateTypeDescription) {
        val model = findModelAggregate(cmd.modelRef)
        val type = model.findTypeOptional(cmd.typeRef) ?: throw TypeNotFoundException(cmd.modelRef, cmd.typeRef)
        if (type.description == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateTypeDescription(model.id, type.id, cmd.value))
    }

    private fun deleteType(cmd: ModelCmd.DeleteType) {
        // Cannot delete type used in any entity
        val model = findModelAggregate(cmd.modelRef)
        val type = model.findTypeOptional(cmd.typeRef) ?: throw TypeNotFoundException(cmd.modelRef, cmd.typeRef)

        val used = model.entities.any { entity -> entity.attributes.any { attr -> attr.typeId == type.id } }
        if (used) throw ModelTypeDeleteUsedException(type.key)

        storage.dispatch(ModelRepoCmd.DeleteType(model.id, type.id))
    }
    // -----------------------------------------------------------------------------------------------------------------
    // Entities
    // -----------------------------------------------------------------------------------------------------------------

    private fun updateEntityKey(cmd: ModelCmd.UpdateEntityKey) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        if (model.entities.any { it.key == cmd.value && it.key != entity.key }) {
            throw EntityUpdateIdDuplicateIdException(entity.key)
        }
        storage.dispatch(ModelRepoCmd.UpdateEntityKey(model.id, entity.id, cmd.value))
    }

    private fun updateEntityName(cmd: ModelCmd.UpdateEntityName) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        if (entity.name == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateEntityName(model.id, entity.id, cmd.value))
    }

    private fun updateEntityDescription(cmd: ModelCmd.UpdateEntityDescription) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        if (entity.description == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateEntityDescription(model.id, entity.id, cmd.value))
    }

    private fun updateEntityIdentifierAttribute(cmd: ModelCmd.UpdateEntityIdentifierAttribute) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attrId = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.value).id
        if (entity.identifierAttributeId == attrId) return
        storage.dispatch(ModelRepoCmd.UpdateEntityIdentifierAttribute(model.id, entity.id, attrId))
    }

    private fun updateEntityDocumentationHome(cmd: ModelCmd.UpdateEntityDocumentationHome) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        if (entity.documentationHome?.toExternalForm() == cmd.value?.toExternalForm()) return
        storage.dispatch(ModelRepoCmd.UpdateEntityDocumentationHome(model.id, entity.id, cmd.value))
    }

    private fun updateEntityTagAdd(cmd: ModelCmd.UpdateEntityTagAdd) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        storage.dispatch(
            ModelRepoCmd.UpdateEntityTagAdd(
                model.id, entity.id,
                tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateEntityTagDelete(cmd: ModelCmd.UpdateEntityTagDelete) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        storage.dispatch(
            ModelRepoCmd.UpdateEntityTagDelete(
                model.id, entity.id,
                tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }


    private fun createEntity(c: ModelCmd.CreateEntity) {
        val model = findModelAggregate(c.modelRef)
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
        val model = findModelAggregate(c.modelRef)
        val entity = findEntity(c.modelRef, c.entityRef)
        storage.dispatch(ModelRepoCmd.DeleteEntity(model.id, entity.id))
    }

    private fun createEntityAttribute(c: ModelCmd.CreateEntityAttribute) {
        val model = findModelAggregate(c.modelRef)
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
        val model = findModelAggregate(cmd.modelRef)
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

    private fun updateEntityAttributeKey(cmd: ModelCmd.UpdateEntityAttributeKey) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attribute = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (entity.attributes.any { it.key == cmd.value && it.key != attribute.key }) {
            throw UpdateAttributeDuplicateKeyException(cmd.entityRef, cmd.attributeRef, cmd.value)
        }
        storage.dispatch(
            ModelRepoCmd.UpdateEntityAttributeKey(model.id, entity.id, attribute.id, cmd.value)
        )
    }

    private fun updateEntityAttributeName(cmd: ModelCmd.UpdateEntityAttributeName) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attribute = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (attribute.name == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateEntityAttributeName(model.id, entity.id, attribute.id, cmd.value))
    }

    private fun updateEntityAttributeDescription(cmd: ModelCmd.UpdateEntityAttributeDescription) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attribute = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (attribute.description == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateEntityAttributeDescription(model.id, entity.id, attribute.id, cmd.value))
    }

    private fun updateEntityAttributeType(cmd: ModelCmd.UpdateEntityAttributeType) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attribute = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        val type = findType(cmd.modelRef, cmd.value)
        if (attribute.typeId == type.id) return
        storage.dispatch(ModelRepoCmd.UpdateEntityAttributeType(model.id, entity.id, attribute.id, type.id))
    }

    private fun updateEntityAttributeOptional(cmd: ModelCmd.UpdateEntityAttributeOptional) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attribute = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (attribute.optional == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateEntityAttributeOptional(model.id, entity.id, attribute.id, cmd.value))
    }

    private fun updateEntityAttributeTagAdd(cmd: ModelCmd.UpdateEntityAttributeTagAdd) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attribute = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.UpdateEntityAttributeTagAdd(
                modelId = model.id,
                entityId = entity.id,
                attributeId = attribute.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateEntityAttributeTagDelete(cmd: ModelCmd.UpdateEntityAttributeTagDelete) {
        val model = findModelAggregate(cmd.modelRef)
        val entity = findEntity(cmd.modelRef, cmd.entityRef)
        val attribute = findEntityAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.UpdateEntityAttributeTagDelete(
                modelId = model.id,
                entityId = entity.id,
                attributeId = attribute.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------


    private fun deleteRelationshipAttribute(cmd: ModelCmd.DeleteRelationshipAttribute) {
        val model = findModelAggregate(cmd.modelRef)
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

    private fun updateRelationshipAttributeKey(cmd: ModelCmd.UpdateRelationshipAttributeKey) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        if (rel.attributes.any { it.key == cmd.value && it.key != attr.key }) {
            throw RelationshipAttributeUpdateDuplicateKeyException(
                cmd.modelRef,
                cmd.relationshipRef,
                cmd.attributeRef,
                cmd.value
            )
        }
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeKey(model.id, rel.id, attr.id, cmd.value)
        )
    }

    private fun updateRelationshipAttributeName(cmd: ModelCmd.UpdateRelationshipAttributeName) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        if (attr.name == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipAttributeName(model.id, rel.id, attr.id, cmd.value))
    }

    private fun updateRelationshipAttributeDescription(cmd: ModelCmd.UpdateRelationshipAttributeDescription) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        if (attr.description == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipAttributeDescription(model.id, rel.id, attr.id, cmd.value))
    }

    private fun updateRelationshipAttributeType(cmd: ModelCmd.UpdateRelationshipAttributeType) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        val type = findType(cmd.modelRef, cmd.value)
        if (attr.typeId == type.id) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipAttributeType(model.id, rel.id, attr.id, type.id))
    }

    private fun updateRelationshipAttributeOptional(cmd: ModelCmd.UpdateRelationshipAttributeOptional) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        if (attr.optional == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipAttributeOptional(model.id, rel.id, attr.id, cmd.value))
    }

    private fun updateRelationshipAttributeTagAdd(cmd: ModelCmd.UpdateRelationshipAttributeTagAdd) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeTagAdd(
                modelId = model.id,
                relationshipId = rel.id,
                attributeId = attr.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateRelationshipAttributeTagDelete(cmd: ModelCmd.UpdateRelationshipAttributeTagDelete) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val attr = findRelationshipAttribute(cmd.modelRef, cmd.relationshipRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeTagDelete(
                modelId = model.id,
                relationshipId = rel.id,
                attributeId = attr.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun deleteRelationship(cmd: ModelCmd.DeleteRelationship) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        storage.dispatch(
            ModelRepoCmd.DeleteRelationship(
                modelId = model.id,
                relationshipId = rel.id,
            )
        )
    }

    private fun updateRelationshipKey(cmd: ModelCmd.UpdateRelationshipKey) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        if (rel.key == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipKey(model.id, rel.id, cmd.value))
    }

    private fun updateRelationshipName(cmd: ModelCmd.UpdateRelationshipName) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        if (rel.name == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipName(model.id, rel.id, cmd.value))
    }

    private fun updateRelationshipDescription(cmd: ModelCmd.UpdateRelationshipDescription) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        if (rel.description == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipDescription(model.id, rel.id, cmd.value))
    }

    private fun updateRelationshipRoleKey(cmd: ModelCmd.UpdateRelationshipRoleKey) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val role = findRelationshipRole(cmd.modelRef, cmd.relationshipRef, cmd.relationshipRoleRef)
        if (role.key == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipRoleKey(model.id, rel.id, role.id, cmd.value))
    }

    private fun updateRelationshipRoleName(cmd: ModelCmd.UpdateRelationshipRoleName) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val role = findRelationshipRole(cmd.modelRef, cmd.relationshipRef, cmd.relationshipRoleRef)
        if (role.name == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipRoleName(model.id, rel.id, role.id, cmd.value))
    }

    private fun updateRelationshipRoleEntity(cmd: ModelCmd.UpdateRelationshipRoleEntity) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val role = findRelationshipRole(cmd.modelRef, cmd.relationshipRef, cmd.relationshipRoleRef)
        val entity = findEntity(cmd.modelRef, cmd.value)
        if (role.entityId == entity.id) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipRoleEntity(model.id, rel.id, role.id, entity.id))
    }

    private fun updateRelationshipRoleCardinality(cmd: ModelCmd.UpdateRelationshipRoleCardinality) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        val role = findRelationshipRole(cmd.modelRef, cmd.relationshipRef, cmd.relationshipRoleRef)
        if (role.cardinality == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipRoleCardinality(model.id, rel.id, role.id, cmd.value))
    }

    private fun updateRelationshipTagAdd(cmd: ModelCmd.UpdateRelationshipTagAdd) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipTagAdd(
                modelId = model.id,
                relationshipId = rel.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateRelationshipTagDelete(cmd: ModelCmd.UpdateRelationshipTagDelete) {
        val model = findModelAggregate(cmd.modelRef)
        val rel = findRelationship(cmd.modelRef, cmd.relationshipRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipTagDelete(
                modelId = model.id,
                relationshipId = rel.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun createRelationshipAttribute(cmd: ModelCmd.CreateRelationshipAttribute) {
        val model = findModelAggregate(cmd.modelRef)
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
        val model = findModelAggregate(cmd.modelRef)
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

    private fun ensureImportedModelIsValid(model: ModelAggregate) {
        when (val validation = modelValidation.validate(model)) {
            is ModelValidationState.Ok -> return
            is ModelValidationState.Error -> throw ModelInvalidException(model.id, validation.errors)
        }
    }


}
