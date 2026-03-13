package io.medatarun.model.internal

import io.medatarun.model.domain.*
import io.medatarun.model.infra.ModelAggregateInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmdEnveloppe
import io.medatarun.model.ports.exposed.ModelCmdOnModel
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.needs.ModelRepoCmd
import io.medatarun.model.ports.needs.ModelStorage
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.model.ports.needs.ModelTagResolver.Companion.modelTagScopeRef
import io.medatarun.platform.db.DbTransactionManager
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.tags.core.domain.TagRef.Companion.tagRefKey

class ModelCmdsImpl(
    private val storage: ModelStorage,
    private val modelValidation: ModelValidation,
    private val auditor: ModelAuditor,
    private val tagResolver: ModelTagResolver,
    private val txManager: DbTransactionManager
) : ModelCmds {

    override fun dispatch(cmdEnv: ModelCmdEnveloppe) {
        val cmd = cmdEnv.cmd
        return txManager.runInTransaction {
            if (cmd is ModelCmdOnModel) ensureModelExists(cmd.modelRef)
            when (cmd) {
                is ModelCmd.CreateModel -> createModel(cmd)
                is ModelCmd.CopyModel -> copyModel(cmd)
                is ModelCmd.ImportModel -> importModel(cmd)
                is ModelCmd.UpdateModelKey -> updateModelKey(cmd)
                is ModelCmd.UpdateModelDescription -> updateModelDescription(cmd)
                is ModelCmd.UpdateModelAuthority -> updateModelAuthority(cmd)
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
                is ModelCmd.CreateRelationshipRole -> createRelationshipRole(cmd)
                is ModelCmd.UpdateRelationshipRoleKey -> updateRelationshipRoleKey(cmd)
                is ModelCmd.UpdateRelationshipRoleName -> updateRelationshipRoleName(cmd)
                is ModelCmd.UpdateRelationshipRoleEntity -> updateRelationshipRoleEntity(cmd)
                is ModelCmd.UpdateRelationshipRoleCardinality -> updateRelationshipRoleCardinality(cmd)
                is ModelCmd.UpdateRelationshipTagAdd -> updateRelationshipTagAdd(cmd)
                is ModelCmd.UpdateRelationshipTagDelete -> updateRelationshipTagDelete(cmd)
                is ModelCmd.DeleteRelationship -> deleteRelationship(cmd)
                is ModelCmd.DeleteRelationshipRole -> deleteRelationshipRole(cmd)
                is ModelCmd.UpdateRelationshipAttributeKey -> updateRelationshipAttributeKey(cmd)
                is ModelCmd.UpdateRelationshipAttributeName -> updateRelationshipAttributeName(cmd)
                is ModelCmd.UpdateRelationshipAttributeDescription -> updateRelationshipAttributeDescription(cmd)
                is ModelCmd.UpdateRelationshipAttributeType -> updateRelationshipAttributeType(cmd)
                is ModelCmd.UpdateRelationshipAttributeOptional -> updateRelationshipAttributeOptional(cmd)
                is ModelCmd.UpdateRelationshipAttributeTagAdd -> updateRelationshipAttributeTagAdd(cmd)
                is ModelCmd.UpdateRelationshipAttributeTagDelete -> updateRelationshipAttributeTagDelete(cmd)
                is ModelCmd.DeleteRelationshipAttribute -> deleteRelationshipAttribute(cmd)
            }
            auditor.onCmdProcessed(cmdEnv)
        }
    }

    // Lookup helpers
    // -----------------------------------------------------------------------------------------------------------------


    data class ModelAndType(val model: Model, val type: ModelType)

    fun findModelAndType(modelRef: ModelRef, typeRef: TypeRef): ModelAndType {
        val model = storage.findModel(modelRef)
        val type = storage.findTypeOptional(model.id, typeRef) ?: throw TypeNotFoundException(modelRef, typeRef)
        return ModelAndType(model, type)
    }

    data class ModelAndEntity(val model: Model, val entity: Entity)

    fun findModelAndEntity(modelRef: ModelRef, entityRef: EntityRef): ModelAndEntity {
        val model = storage.findModel(modelRef)
        val entity = storage.findEntity(model.id, entityRef)
        return ModelAndEntity(model, entity)
    }

    data class ModelAndEntityAndAttribute(val model: Model, val entity: Entity, val attribute: Attribute)

    fun findModelAndEntityAndAttribute(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attributeRef: EntityAttributeRef
    ): ModelAndEntityAndAttribute {
        val model = storage.findModel(modelRef)
        val entity = storage.findEntity(model.id, entityRef)
        val attribute = storage.findEntityAttribute(model.id, entity.id, attributeRef)
        return ModelAndEntityAndAttribute(model, entity, attribute)
    }

    data class ModelAndRelationship(val model: Model, val relationship: Relationship)

    fun findModelAndRelationship(modelRef: ModelRef, relationshipRef: RelationshipRef): ModelAndRelationship {
        val model = storage.findModel(modelRef)
        val relationship = storage.findRelationship(model.id, relationshipRef)
        return ModelAndRelationship(model, relationship)
    }

    data class ModelAndRelationshipAndAttribute(
        val model: Model,
        val relationship: Relationship,
        val attribute: Attribute
    )

    fun findModelAndRelationshipAndAttribute(
        modelRef: ModelRef,
        relationshipRef: RelationshipRef,
        attributeRef: RelationshipAttributeRef
    ): ModelAndRelationshipAndAttribute {
        val model = storage.findModel(modelRef)
        val relationship = storage.findRelationship(model.id, relationshipRef)
        val attribute = storage.findRelationshipAttribute(model.id, relationship.id, attributeRef)
        return ModelAndRelationshipAndAttribute(model, relationship, attribute)
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Model
    // -----------------------------------------------------------------------------------------------------------------


    private fun createModel(cmd: ModelCmd.CreateModel) {
        val model = ModelInMemory(
            id = ModelId.generate(),
            key = cmd.modelKey,
            name = cmd.name,
            description = cmd.description,
            version = cmd.version,
            origin = ModelOrigin.Manual,
            authority = ModelAuthority.SYSTEM,
            documentationHome = null,
        )
        storage.dispatch(ModelRepoCmd.CreateModel(model))
    }


    private fun copyModel(cmd: ModelCmd.CopyModel) {
        val source = storage.findModelAggregate(cmd.modelRef)
        if (storage.existsModelByKey(cmd.modelNewKey)) throw ModelDuplicateKeyException(cmd.modelNewKey)
        val copied = ModelCmdCopyImpl().copy(source, cmd.modelNewKey)
        storage.dispatch(ModelRepoCmd.StoreModelAggregate(copied))

        val copiedModelRef = ModelRef.ById(copied.id)
        val sourceScopeRef = modelTagScopeRef(source.id)
        val copiedScopeRef = modelTagScopeRef(copied.id)
        val copiedTagIdsBySourceTagId = mutableMapOf<TagId, TagId>()

        /**
         * For tags local to source model scope, create an equivalent local tag in copied model scope.
         * For all other scopes, keep the same tag id.
         */
        fun mapTagId(sourceTagId: TagId): TagId {
            val existing = copiedTagIdsBySourceTagId[sourceTagId]
            if (existing != null) return existing

            val sourceTag = tagResolver.findTagById(sourceTagId)
            val mappedTagId = if (sourceTag.scope == sourceScopeRef) {
                tagResolver.create(copied.id, sourceTag.key, sourceTag.name, sourceTag.description)
                tagResolver.resolveTagId(TagRef.ByKey(copiedScopeRef, null, sourceTag.key))
            } else {
                sourceTagId
            }
            copiedTagIdsBySourceTagId[sourceTagId] = mappedTagId
            return mappedTagId
        }

        source.tags.forEach { sourceTagId ->
            updateModelTagAdd(
                ModelCmd.UpdateModelTagAdd(
                    copiedModelRef,
                    TagRef.ById(mapTagId(sourceTagId))
                )
            )
        }

        source.entities.forEach { sourceEntity ->
            val copiedEntity = copied.findEntity(sourceEntity.key)
            sourceEntity.tags.forEach { sourceTagId ->
                updateEntityTagAdd(
                    ModelCmd.UpdateEntityTagAdd(
                        copiedModelRef,
                        EntityRef.ById(copiedEntity.id),
                        TagRef.ById(mapTagId(sourceTagId))
                    )
                )
            }
        }

        source.relationships.forEach { sourceRelationship ->
            val copiedRelationship = copied.findRelationship(RelationshipRef.ByKey(sourceRelationship.key))
            sourceRelationship.tags.forEach { sourceTagId ->
                updateRelationshipTagAdd(
                    ModelCmd.UpdateRelationshipTagAdd(
                        copiedModelRef,
                        RelationshipRef.ById(copiedRelationship.id),
                        TagRef.ById(mapTagId(sourceTagId))
                    )
                )
            }
        }

        source.attributes.forEach { sourceAttribute ->
            val sourceOwner = sourceAttribute.ownerId
            if (sourceOwner is AttributeOwnerId.OwnerEntityId) {
                val sourceEntity = source.findEntity(sourceOwner.id)
                val copiedEntity = copied.findEntity(sourceEntity.key)
                val copiedAttribute = copied.findEntityAttribute(
                    EntityRef.ById(copiedEntity.id),
                    EntityAttributeRef.ByKey(sourceAttribute.key)
                )
                sourceAttribute.tags.forEach { sourceTagId ->
                    updateEntityAttributeTagAdd(
                        ModelCmd.UpdateEntityAttributeTagAdd(
                            copiedModelRef,
                            EntityRef.ById(copiedEntity.id),
                            EntityAttributeRef.ById(copiedAttribute.id),
                            TagRef.ById(mapTagId(sourceTagId))
                        )
                    )
                }
            }
            if (sourceOwner is AttributeOwnerId.OwnerRelationshipId) {
                val sourceRelationship = source.findRelationship(sourceOwner.id)
                val copiedRelationship = copied.findRelationship(RelationshipRef.ByKey(sourceRelationship.key))
                val copiedAttribute = copied.findRelationshipAttributeOptional(
                    RelationshipRef.ById(copiedRelationship.id),
                    RelationshipAttributeRef.ByKey(sourceAttribute.key)
                ) ?: throw RelationshipAttributeNotFoundException(
                    copiedModelRef,
                    RelationshipRef.ById(copiedRelationship.id),
                    RelationshipAttributeRef.ByKey(sourceAttribute.key)
                )
                sourceAttribute.tags.forEach { sourceTagId ->
                    updateRelationshipAttributeTagAdd(
                        ModelCmd.UpdateRelationshipAttributeTagAdd(
                            copiedModelRef,
                            RelationshipRef.ById(copiedRelationship.id),
                            RelationshipAttributeRef.ById(copiedAttribute.id),
                            TagRef.ById(mapTagId(sourceTagId))
                        )
                    )
                }
            }
        }
    }

    private fun importModel(cmd: ModelCmd.ImportModel) {
        // TODO handle tags ?

        // Makes sure that imported models are always with a SYSTEM authority
        val model = ModelAggregateInMemory.of(cmd.model).copy(
            model = ModelInMemory.of(cmd.model).copy(authority = ModelAuthority.SYSTEM)
        )

        if (storage.existsModelByKey(model.key)) throw ModelDuplicateKeyException(model.key)
        ensureImportedModelIsValid(model)
        storage.dispatch(ModelRepoCmd.StoreModelAggregate(model))

        val newtags = cmd.tags

        // Register each found tag
        newtags.forEach { tag -> tagResolver.create(cmd.model.id, tag.key, tag.name, tag.description) }

        // Read the model and temporary tag ids inside, then apply the tags to model elements
        fun applyTags(tagIds: List<TagId>, block: (tagRef: TagRef.ByKey) -> Unit) {
            for (tagId in tagIds) {
                val tagKey = newtags.firstOrNull { it.id == tagId }?.key
                if (tagKey != null) {
                    val tagRef = tagRefKey(modelTagScopeRef(model.id), null, tagKey)
                    block(tagRef)
                }
            }
        }

        applyTags(model.tags) { tagRef ->
            updateModelTagAdd(
                ModelCmd.UpdateModelTagAdd(
                    ModelRef.ById(model.id),
                    tagRef
                )
            )
        }

        for (entity in model.entities) {
            applyTags(entity.tags) { tagRef ->
                updateEntityTagAdd(
                    ModelCmd.UpdateEntityTagAdd(
                        ModelRef.ById(model.id),
                        EntityRef.ById(entity.id), tagRef
                    )
                )
            }
        }

        for (relationship in model.relationships) {
            applyTags(relationship.tags) { tagRef ->
                updateRelationshipTagAdd(
                    ModelCmd.UpdateRelationshipTagAdd(
                        ModelRef.ById(model.id),
                        RelationshipRef.ById(relationship.id),
                        tagRef
                    )
                )
            }
        }

        for (attribute in model.attributes) {
            applyTags(attribute.tags) { tagRef ->
                val owner = attribute.ownerId
                when (owner) {
                    is AttributeOwnerId.OwnerEntityId -> {
                        updateEntityAttributeTagAdd(
                            ModelCmd.UpdateEntityAttributeTagAdd(
                                ModelRef.ById(model.id),
                                EntityRef.ById(owner.id),
                                EntityAttributeRef.ById(attribute.id),
                                tagRef
                            )
                        )
                    }

                    is AttributeOwnerId.OwnerRelationshipId -> {
                        updateRelationshipAttributeTagAdd(
                            ModelCmd.UpdateRelationshipAttributeTagAdd(
                                ModelRef.ById(model.id),
                                RelationshipRef.ById(owner.id),
                                RelationshipAttributeRef.ById(attribute.id),
                                tagRef
                            )
                        )
                    }
                }

            }
        }


    }


    private fun deleteModel(cmd: ModelCmd.DeleteModel) {
        val model = storage.findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.DeleteModel(model.id))
    }

    private fun updateModelName(cmd: ModelCmd.UpdateModelName) {
        val model = storage.findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelName(model.id, cmd.name))
    }

    private fun updateModelKey(cmd: ModelCmd.UpdateModelKey) {
        val model = storage.findModel(cmd.modelRef)
        if (model.key != cmd.key && storage.existsModelByKey(cmd.key)) {
            throw ModelDuplicateKeyException(cmd.key)
        }
        storage.dispatch(ModelRepoCmd.UpdateModelKey(model.id, cmd.key))
    }

    private fun updateModelDescription(cmd: ModelCmd.UpdateModelDescription) {
        val model = storage.findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelDescription(model.id, cmd.description))
    }

    private fun updateModelAuthority(cmd: ModelCmd.UpdateModelAuthority) {
        val model = storage.findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelAuthority(model.id, cmd.authority))
    }

    private fun updateModelVersion(cmd: ModelCmd.UpdateModelVersion) {
        val model = storage.findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelVersion(model.id, cmd.version))
    }

    private fun updateDocumentationHome(cmd: ModelCmd.UpdateModelDocumentationHome) {
        val model = storage.findModel(cmd.modelRef)
        storage.dispatch(ModelRepoCmd.UpdateModelDocumentationHome(model.id, cmd.url))
    }

    private fun updateModelTagAdd(cmd: ModelCmd.UpdateModelTagAdd) {
        val model = storage.findModel(cmd.modelRef)
        storage.dispatch(
            ModelRepoCmd.UpdateModelTagAdd(
                model.id,
                tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateModelTagDelete(cmd: ModelCmd.UpdateModelTagDelete) {
        val model = storage.findModel(cmd.modelRef)
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
        val model = storage.findModel(cmd.modelRef)
        val existing = storage.findTypeByKeyOptional(model.id, cmd.initializer.key)
        if (existing != null) throw TypeCreateDuplicateException(model.key, cmd.initializer.key)
        storage.dispatch(ModelRepoCmd.CreateType(model.id, cmd.initializer))
    }

    private fun updateTypeKey(cmd: ModelCmd.UpdateTypeKey) {
        val (model, type) = findModelAndType(cmd.modelRef, cmd.typeRef)
        if (type.key == cmd.value) return
        val found = storage.findTypeByKeyOptional(model.id, cmd.value)
        if (found != null) throw TypeUpdateDuplicateKeyException(cmd.value)
        storage.dispatch(ModelRepoCmd.UpdateTypeKey(model.id, type.id, cmd.value))
    }

    private fun updateTypeName(cmd: ModelCmd.UpdateTypeName) {
        val (model, type) = findModelAndType(cmd.modelRef, cmd.typeRef)
        if (type.name == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateTypeName(model.id, type.id, cmd.value))
    }

    private fun updateTypeDescription(cmd: ModelCmd.UpdateTypeDescription) {
        val (model, type) = findModelAndType(cmd.modelRef, cmd.typeRef)
        if (type.description == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateTypeDescription(model.id, type.id, cmd.value))
    }

    private fun deleteType(cmd: ModelCmd.DeleteType) {
        val (model, type) = findModelAndType(cmd.modelRef, cmd.typeRef)

        val used = storage.isTypeUsedInEntityAttributes(model.id, type.id)
                || storage.isTypeUsedInRelationshipAttributes(model.id, type.id)

        if (used) throw ModelTypeDeleteUsedException(type.key)

        storage.dispatch(ModelRepoCmd.DeleteType(model.id, type.id))
    }
    // -----------------------------------------------------------------------------------------------------------------
    // Entities
    // -----------------------------------------------------------------------------------------------------------------


    private fun updateEntityKey(cmd: ModelCmd.UpdateEntityKey) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        val duplicate = storage.findEntityByKeyOptional(model.id, cmd.value)

        if (duplicate != null && duplicate.id != entity.id) {
            throw EntityUpdateKeyDuplicateKeyException(entity.key)
        }

        storage.dispatch(ModelRepoCmd.UpdateEntityKey(model.id, entity.id, cmd.value))
    }

    private fun updateEntityName(cmd: ModelCmd.UpdateEntityName) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        if (entity.name == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateEntityName(model.id, entity.id, cmd.value))
    }

    private fun updateEntityDescription(cmd: ModelCmd.UpdateEntityDescription) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        if (entity.description == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateEntityDescription(model.id, entity.id, cmd.value))
    }

    private fun updateEntityIdentifierAttribute(cmd: ModelCmd.UpdateEntityIdentifierAttribute) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.value)
        val attrId = attribute.id
        if (entity.identifierAttributeId == attrId) return
        storage.dispatch(ModelRepoCmd.UpdateEntityIdentifierAttribute(model.id, entity.id, attrId))
    }

    private fun updateEntityDocumentationHome(cmd: ModelCmd.UpdateEntityDocumentationHome) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        if (entity.documentationHome?.toExternalForm() == cmd.value?.toExternalForm()) return
        storage.dispatch(ModelRepoCmd.UpdateEntityDocumentationHome(model.id, entity.id, cmd.value))
    }

    private fun updateEntityTagAdd(cmd: ModelCmd.UpdateEntityTagAdd) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        storage.dispatch(
            ModelRepoCmd.UpdateEntityTagAdd(
                model.id, entity.id,
                tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateEntityTagDelete(cmd: ModelCmd.UpdateEntityTagDelete) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        storage.dispatch(
            ModelRepoCmd.UpdateEntityTagDelete(
                model.id, entity.id,
                tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }


    private fun createEntity(c: ModelCmd.CreateEntity) {
        val model = storage.findModel(c.modelRef)
        val type = storage.findType(model.id, c.entityInitializer.identityAttribute.type)
        val identityAttributeId = AttributeId.generate()
        storage.dispatch(
            ModelRepoCmd.CreateEntity(
                modelId = model.id,
                entityId = EntityId.generate(),
                key = c.entityInitializer.entityKey,
                name = c.entityInitializer.name,
                description = c.entityInitializer.description,
                origin = EntityOrigin.Manual,
                documentationHome = c.entityInitializer.documentationHome,
                identityAttributeId = identityAttributeId,
                identityAttributeKey = c.entityInitializer.identityAttribute.attributeKey,
                identityAttributeName = c.entityInitializer.identityAttribute.name,
                identityAttributeDescription = c.entityInitializer.identityAttribute.description,
                identityAttributeTypeId = type.id,
                identityAttributeIdOptional = false, // because it's identity, can never be optional

            )
        )
    }

    private fun deleteEntity(cmd: ModelCmd.DeleteEntity) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        storage.dispatch(ModelRepoCmd.DeleteEntity(model.id, entity.id))
    }

    private fun createEntityAttribute(cmd: ModelCmd.CreateEntityAttribute) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        val found = storage.findEntityAttributeByKeyOptional(model.id, entity.id, cmd.attributeInitializer.attributeKey)
        if (found != null)
            throw CreateAttributeDuplicateKeyException(entity.key, cmd.attributeInitializer.attributeKey)

        // Makes sure the type reference exists, even if now, the type is referenced by a key only
        val typeRef = cmd.attributeInitializer.type
        val type = storage.findType(model.id, typeRef)

        storage.dispatch(
            ModelRepoCmd.CreateEntityAttribute(
                modelId = model.id,
                entityId = entity.id,
                attributeId = AttributeId.generate(),
                key = cmd.attributeInitializer.attributeKey,
                name = cmd.attributeInitializer.name,
                description = cmd.attributeInitializer.description,
                typeId = type.id,
                optional = cmd.attributeInitializer.optional,
            )
        )
    }

    private fun deleteEntityAttribute(cmd: ModelCmd.DeleteEntityAttribute) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (entity.identifierAttributeId == attribute.id)
            throw DeleteAttributeIdentifierException(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        storage.dispatch(
            ModelRepoCmd.DeleteEntityAttribute(
                modelId = model.id,
                entityId = entity.id,
                attributeId = attribute.id
            )
        )
    }

    private fun updateEntityAttributeKey(cmd: ModelCmd.UpdateEntityAttributeKey) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        val found = storage.findEntityAttributeByKeyOptional(model.id, entity.id, cmd.value)
        if (found != null && found.id != attribute.id) throw UpdateAttributeDuplicateKeyException(
            cmd.entityRef,
            cmd.attributeRef,
            cmd.value
        )
        storage.dispatch(
            ModelRepoCmd.UpdateEntityAttributeKey(model.id, entity.id, attribute.id, cmd.value)
        )
    }

    private fun updateEntityAttributeName(cmd: ModelCmd.UpdateEntityAttributeName) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (attribute.name == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateEntityAttributeName(model.id, entity.id, attribute.id, cmd.value))
    }

    private fun updateEntityAttributeDescription(cmd: ModelCmd.UpdateEntityAttributeDescription) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (attribute.description == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateEntityAttributeDescription(model.id, entity.id, attribute.id, cmd.value))
    }

    private fun updateEntityAttributeType(cmd: ModelCmd.UpdateEntityAttributeType) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        val type = storage.findType(model.id, cmd.value)
        if (attribute.typeId == type.id) return
        storage.dispatch(ModelRepoCmd.UpdateEntityAttributeType(model.id, entity.id, attribute.id, type.id))
    }

    private fun updateEntityAttributeOptional(cmd: ModelCmd.UpdateEntityAttributeOptional) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (attribute.optional == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateEntityAttributeOptional(model.id, entity.id, attribute.id, cmd.value))
    }

    private fun updateEntityAttributeTagAdd(cmd: ModelCmd.UpdateEntityAttributeTagAdd) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
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
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
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
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        storage.dispatch(
            ModelRepoCmd.DeleteRelationshipAttribute(
                modelId = model.id,
                relationshipId = relationship.id,
                attributeId = attribute.id
            )
        )
    }

    private fun updateRelationshipAttributeKey(cmd: ModelCmd.UpdateRelationshipAttributeKey) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        val found = storage.findRelationshipAttributeByKeyOptional(model.id, relationship.id, cmd.value)
        if (found != null && found.id != attribute.id) throw RelationshipAttributeUpdateDuplicateKeyException(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef,
            cmd.value
        )
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeKey(model.id, relationship.id, attribute.id, cmd.value)
        )
    }

    private fun updateRelationshipAttributeName(cmd: ModelCmd.UpdateRelationshipAttributeName) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        if (attribute.name == cmd.value) return
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeName(
                model.id,
                relationship.id,
                attribute.id,
                cmd.value
            )
        )
    }

    private fun updateRelationshipAttributeDescription(cmd: ModelCmd.UpdateRelationshipAttributeDescription) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        if (attribute.description == cmd.value) return
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeDescription(
                model.id,
                relationship.id,
                attribute.id,
                cmd.value
            )
        )
    }

    private fun updateRelationshipAttributeType(cmd: ModelCmd.UpdateRelationshipAttributeType) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        val type = storage.findType(model.id, cmd.value)
        if (attribute.typeId == type.id) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipAttributeType(model.id, relationship.id, attribute.id, type.id))
    }

    private fun updateRelationshipAttributeOptional(cmd: ModelCmd.UpdateRelationshipAttributeOptional) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        if (attribute.optional == cmd.value) return
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeOptional(
                model.id,
                relationship.id,
                attribute.id,
                cmd.value
            )
        )
    }

    private fun updateRelationshipAttributeTagAdd(cmd: ModelCmd.UpdateRelationshipAttributeTagAdd) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeTagAdd(
                modelId = model.id,
                relationshipId = relationship.id,
                attributeId = attribute.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateRelationshipAttributeTagDelete(cmd: ModelCmd.UpdateRelationshipAttributeTagDelete) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipAttributeTagDelete(
                modelId = model.id,
                relationshipId = relationship.id,
                attributeId = attribute.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun deleteRelationship(cmd: ModelCmd.DeleteRelationship) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        storage.dispatch(
            ModelRepoCmd.DeleteRelationship(
                modelId = model.id,
                relationshipId = relationship.id,
            )
        )
    }

    private fun updateRelationshipKey(cmd: ModelCmd.UpdateRelationshipKey) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        if (relationship.key == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipKey(model.id, relationship.id, cmd.value))
    }

    private fun updateRelationshipName(cmd: ModelCmd.UpdateRelationshipName) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        if (relationship.name == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipName(model.id, relationship.id, cmd.value))
    }

    private fun updateRelationshipDescription(cmd: ModelCmd.UpdateRelationshipDescription) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        if (relationship.description == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipDescription(model.id, relationship.id, cmd.value))
    }

    private fun createRelationshipRole(cmd: ModelCmd.CreateRelationshipRole) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        val duplicate = storage.findRelationshipRoleByKeyOptional(model.id, relationship.id, cmd.key)
        if (duplicate != null) {
            throw RelationshipRoleCreateDuplicateKeyException(cmd.modelRef, cmd.relationshipRef, cmd.key)
        }
        val entity = storage.findEntity(model.id, cmd.entityRef)
        storage.dispatch(
            ModelRepoCmd.CreateRelationshipRole(
                modelId = model.id,
                relationshipId = relationship.id,
                relationshipRoleId = RelationshipRoleId.generate(),
                key = cmd.key,
                entityId = entity.id,
                name = cmd.name,
                cardinality = cmd.cardinality
            )
        )
    }

    private fun updateRelationshipRoleKey(cmd: ModelCmd.UpdateRelationshipRoleKey) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        val role = storage.findRelationshipRole(model.id, relationship.id, cmd.relationshipRoleRef)
        val duplicate = storage.findRelationshipRoleByKeyOptional(model.id, relationship.id, cmd.value)
        if (duplicate != null && duplicate.id != role.id) {
            throw RelationshipRoleUpdateDuplicateKeyException(
                cmd.modelRef,
                cmd.relationshipRef,
                cmd.relationshipRoleRef,
                cmd.value
            )
        }
        if (role.key == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipRoleKey(model.id, relationship.id, role.id, cmd.value))
    }

    private fun updateRelationshipRoleName(cmd: ModelCmd.UpdateRelationshipRoleName) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        val role = storage.findRelationshipRole(model.id, relationship.id, cmd.relationshipRoleRef)
        if (role.name == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipRoleName(model.id, relationship.id, role.id, cmd.value))
    }

    private fun updateRelationshipRoleEntity(cmd: ModelCmd.UpdateRelationshipRoleEntity) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        val role = storage.findRelationshipRole(model.id, relationship.id, cmd.relationshipRoleRef)
        val entity = storage.findEntity(model.id, cmd.value)
        if (role.entityId == entity.id) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipRoleEntity(model.id, relationship.id, role.id, entity.id))
    }

    private fun updateRelationshipRoleCardinality(cmd: ModelCmd.UpdateRelationshipRoleCardinality) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        val role = storage.findRelationshipRole(model.id, relationship.id, cmd.relationshipRoleRef)
        if (role.cardinality == cmd.value) return
        storage.dispatch(ModelRepoCmd.UpdateRelationshipRoleCardinality(model.id, relationship.id, role.id, cmd.value))
    }

    private fun deleteRelationshipRole(cmd: ModelCmd.DeleteRelationshipRole) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        if (relationship.roles.size <= 2) {
            throw RelationshipRoleDeleteMinimumRolesException(cmd.modelRef, cmd.relationshipRef)
        }
        val role = storage.findRelationshipRole(model.id, relationship.id, cmd.relationshipRoleRef)
        storage.dispatch(
            ModelRepoCmd.DeleteRelationshipRole(
                modelId = model.id,
                relationshipId = relationship.id,
                relationshipRoleId = role.id
            )
        )
    }

    private fun updateRelationshipTagAdd(cmd: ModelCmd.UpdateRelationshipTagAdd) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipTagAdd(
                modelId = model.id,
                relationshipId = relationship.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateRelationshipTagDelete(cmd: ModelCmd.UpdateRelationshipTagDelete) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        storage.dispatch(
            ModelRepoCmd.UpdateRelationshipTagDelete(
                modelId = model.id,
                relationshipId = relationship.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun createRelationshipAttribute(cmd: ModelCmd.CreateRelationshipAttribute) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        val exists = storage.findRelationshipAttributeByKeyOptional(model.id, relationship.id, cmd.attr.attributeKey)
        if (exists != null) {
            throw RelationshipAttributeCreateDuplicateKeyException(
                cmd.modelRef,
                cmd.relationshipRef,
                cmd.attr.attributeKey
            )
        }
        val type = storage.findType(model.id, cmd.attr.type)
        storage.dispatch(
            ModelRepoCmd.CreateRelationshipAttribute(
                modelId = model.id,
                relationshipId = relationship.id,
                attributeId = AttributeId.generate(),
                key = cmd.attr.attributeKey,
                name = cmd.attr.name,
                description = cmd.attr.description,
                typeId = type.id,
                optional = cmd.attr.optional,
            )
        )
    }


    private fun createRelationship(cmd: ModelCmd.CreateRelationship) {
        val model = storage.findModel(cmd.modelRef)
        val duplicateKey = storage.findRelationshipByKeyOptional(model.id, cmd.initializer.key)

        if (duplicateKey != null)
            throw RelationshipDuplicateIdException(model.id, cmd.initializer.key)

        val duplicateRoleIds =
            cmd.initializer.roles.groupBy { it.key }.mapValues { it.value.size }.filter { it.value > 1 }
        if (duplicateRoleIds.isNotEmpty()) {
            throw RelationshipDuplicateRoleIdException(duplicateRoleIds.keys)
        }

        storage.dispatch(
            ModelRepoCmd.CreateRelationship(
                modelId = model.id,
                relationshipId = RelationshipId.generate(),
                name = cmd.initializer.name,
                description = cmd.initializer.description,
                key = cmd.initializer.key,
                roles = cmd.initializer.roles.map {
                    val entity = storage.findEntity(model.id, it.entityRef)
                    ModelRepoCmd.RelationshipRoleInitializer(
                        id = RelationshipRoleId.generate(),
                        key = it.key,
                        entityId = entity.id,
                        name = it.name,
                        cardinality = it.cardinality
                    )
                }
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
