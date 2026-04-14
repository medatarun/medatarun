package io.medatarun.model.internal

import io.medatarun.model.domain.*
import io.medatarun.model.infra.ModelAggregateInMemory
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmdEnveloppe
import io.medatarun.model.ports.exposed.ModelCmdOnModel
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.needs.*
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

    private val modelCopyDelegate = ModelCmdCopyImpl(tagResolver)

    private fun storageCmdEnveloppe(cmdEnv: ModelCmdEnveloppe, repoCmd: ModelStorageCmd): ModelStorageCmdEnveloppe {
        return ModelStorageCmdEnveloppe(
            traceabilityRecord = cmdEnv.traceabilityRecord,
            cmd = repoCmd
        )
    }

    private fun storageDispatch(cmdEnv: ModelCmdEnveloppe, repoCmd: ModelStorageCmd) {
        storage.dispatch(storageCmdEnveloppe(cmdEnv, repoCmd))
    }

    override fun dispatch(cmdEnv: ModelCmdEnveloppe) {
        val cmd = cmdEnv.cmd
        return txManager.runInTransaction {
            if (cmd is ModelCmdOnModel) ensureModelExists(cmd.modelRef)
            when (cmd) {
                is ModelCmd.CreateModel -> createModel(cmdEnv, cmd)
                is ModelCmd.CopyModel -> copyModel(cmdEnv, cmd)
                is ModelCmd.ImportModel -> importModel(cmdEnv, cmd)
                is ModelCmd.UpdateModelKey -> updateModelKey(cmdEnv, cmd)
                is ModelCmd.UpdateModelDescription -> updateModelDescription(cmdEnv, cmd)
                is ModelCmd.UpdateModelAuthority -> updateModelAuthority(cmdEnv, cmd)
                is ModelCmd.UpdateModelName -> updateModelName(cmdEnv, cmd)
                is ModelCmd.ModelRelease -> cmdModelRelease(cmdEnv, cmd)
                is ModelCmd.UpdateModelDocumentationHome -> updateDocumentationHome(cmdEnv, cmd)
                is ModelCmd.UpdateModelTagAdd -> updateModelTagAdd(cmdEnv, cmd)
                is ModelCmd.UpdateModelTagDelete -> updateModelTagDelete(cmdEnv, cmd)
                is ModelCmd.RemoveTagReferences -> removeTagReferences(cmdEnv, cmd)
                is ModelCmd.DeleteModel -> deleteModel(cmdEnv, cmd)
                is ModelCmd.CreateType -> createType(cmdEnv, cmd)
                is ModelCmd.UpdateTypeKey -> updateTypeKey(cmdEnv, cmd)
                is ModelCmd.UpdateTypeName -> updateTypeName(cmdEnv, cmd)
                is ModelCmd.UpdateTypeDescription -> updateTypeDescription(cmdEnv, cmd)
                is ModelCmd.DeleteType -> deleteType(cmdEnv, cmd)
                is ModelCmd.CreateEntity -> createEntity(cmdEnv, cmd)
                is ModelCmd.UpdateEntityKey -> updateEntityKey(cmdEnv, cmd)
                is ModelCmd.UpdateEntityName -> updateEntityName(cmdEnv, cmd)
                is ModelCmd.UpdateEntityDescription -> updateEntityDescription(cmdEnv, cmd)
                is ModelCmd.UpdateEntityPrimaryKey -> updateEntityPrimaryKey(cmdEnv, cmd)
                is ModelCmd.BusinessKeyCreate -> createBusinessKey(cmdEnv, cmd)
                is ModelCmd.UpdateEntityDocumentationHome -> updateEntityDocumentationHome(cmdEnv, cmd)
                is ModelCmd.UpdateEntityTagAdd -> updateEntityTagAdd(cmdEnv, cmd)
                is ModelCmd.UpdateEntityTagDelete -> updateEntityTagDelete(cmdEnv, cmd)
                is ModelCmd.DeleteEntity -> deleteEntity(cmdEnv, cmd)
                is ModelCmd.CreateEntityAttribute -> createEntityAttribute(cmdEnv, cmd)
                is ModelCmd.UpdateEntityAttributeKey -> updateEntityAttributeKey(cmdEnv, cmd)
                is ModelCmd.UpdateEntityAttributeName -> updateEntityAttributeName(cmdEnv, cmd)
                is ModelCmd.UpdateEntityAttributeDescription -> updateEntityAttributeDescription(cmdEnv, cmd)
                is ModelCmd.UpdateEntityAttributeType -> updateEntityAttributeType(cmdEnv, cmd)
                is ModelCmd.UpdateEntityAttributeOptional -> updateEntityAttributeOptional(cmdEnv, cmd)
                is ModelCmd.UpdateEntityAttributeTagAdd -> updateEntityAttributeTagAdd(cmdEnv, cmd)
                is ModelCmd.UpdateEntityAttributeTagDelete -> updateEntityAttributeTagDelete(cmdEnv, cmd)
                is ModelCmd.DeleteEntityAttribute -> deleteEntityAttribute(cmdEnv, cmd)
                is ModelCmd.CreateRelationship -> createRelationship(cmdEnv, cmd)
                is ModelCmd.CreateRelationshipAttribute -> createRelationshipAttribute(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipKey -> updateRelationshipKey(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipName -> updateRelationshipName(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipDescription -> updateRelationshipDescription(cmdEnv, cmd)
                is ModelCmd.CreateRelationshipRole -> createRelationshipRole(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipRoleKey -> updateRelationshipRoleKey(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipRoleName -> updateRelationshipRoleName(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipRoleEntity -> updateRelationshipRoleEntity(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipRoleCardinality -> updateRelationshipRoleCardinality(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipTagAdd -> updateRelationshipTagAdd(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipTagDelete -> updateRelationshipTagDelete(cmdEnv, cmd)
                is ModelCmd.DeleteRelationship -> deleteRelationship(cmdEnv, cmd)
                is ModelCmd.DeleteRelationshipRole -> deleteRelationshipRole(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipAttributeKey -> updateRelationshipAttributeKey(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipAttributeName -> updateRelationshipAttributeName(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipAttributeDescription -> updateRelationshipAttributeDescription(
                    cmdEnv,
                    cmd
                )

                is ModelCmd.UpdateRelationshipAttributeType -> updateRelationshipAttributeType(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipAttributeOptional -> updateRelationshipAttributeOptional(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipAttributeTagAdd -> updateRelationshipAttributeTagAdd(cmdEnv, cmd)
                is ModelCmd.UpdateRelationshipAttributeTagDelete -> updateRelationshipAttributeTagDelete(cmdEnv, cmd)
                is ModelCmd.DeleteRelationshipAttribute -> deleteRelationshipAttribute(cmdEnv, cmd)
            }
            auditor.onCmdProcessed(cmdEnv)
        }
    }

    override fun maintenanceRebuildCaches() {
        return txManager.runInTransaction {
            storage.maintenanceRebuildCaches()
        }
    }

    private fun removeTagReferences(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.RemoveTagReferences) {
        val locations = storage.findDomainTagLocationsByTagId(cmd.tagId)
        locations.forEach { location ->
            when (location) {
                is DomainTagLocation.Model -> {
                    storageDispatch(
                        cmdEnv,
                        ModelStorageCmd.UpdateModelTagDelete(location.modelId, cmd.tagId)
                    )
                }

                is DomainTagLocation.Entity -> {
                    storageDispatch(
                        cmdEnv,
                        ModelStorageCmd.UpdateEntityTagDelete(
                            location.modelId,
                            location.entityId,
                            cmd.tagId
                        )
                    )
                }

                is DomainTagLocation.EntityAttribute -> {
                    storageDispatch(
                        cmdEnv,
                        ModelStorageCmd.UpdateEntityAttributeTagDelete(
                            location.modelId,
                            location.entityId,
                            location.attributeId,
                            cmd.tagId
                        )
                    )
                }

                is DomainTagLocation.Relationship -> {
                    storageDispatch(
                        cmdEnv,
                        ModelStorageCmd.UpdateRelationshipTagDelete(
                            location.modelId,
                            location.relationshipId,
                            cmd.tagId
                        )
                    )
                }

                is DomainTagLocation.RelationshipAttribute -> {
                    storageDispatch(
                        cmdEnv,
                        ModelStorageCmd.UpdateRelationshipAttributeTagDelete(
                            location.modelId,
                            location.relationshipId,
                            location.attributeId,
                            cmd.tagId
                        )
                    )
                }
            }
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


    private fun createModel(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.CreateModel) {
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
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.CreateModel(
                id = model.id,
                key = model.key,
                name = model.name,
                description = model.description,
                version = model.version,
                origin = model.origin,
                authority = model.authority,
                documentationHome = model.documentationHome
            )
        )
        storageDispatch(cmdEnv, ModelStorageCmd.ModelRelease(model.id, model.version))
    }


    private fun copyModel(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.CopyModel) {
        val source = storage.findModelAggregate(cmd.modelRef)
        if (storage.existsModelByKey(cmd.modelNewKey)) throw ModelDuplicateKeyException(cmd.modelNewKey)
        val copied = modelCopyDelegate.copyAndRetag(
            cmdEnv = cmdEnv,
            source = source,
            modelNewKey = cmd.modelNewKey,
            persistCopied = { aggregate ->
                storageDispatch(
                    cmdEnv,
                    StoreModelAggregatePayloadFactory.create(aggregate)
                )
            },
            tagWriter = createTagWriter(cmdEnv)
        )
        storageDispatch(cmdEnv, ModelStorageCmd.ModelRelease(copied.id, copied.version))

    }

    private fun createTagWriter(cmdEnv: ModelCmdEnveloppe): ModelTaggerImpl.ModelTagWriter {
        return object : ModelTaggerImpl.ModelTagWriter {
            override fun addModelTag(cmd: ModelCmd.UpdateModelTagAdd) {
                updateModelTagAdd(cmdEnv, cmd)
            }

            override fun addEntityTag(cmd: ModelCmd.UpdateEntityTagAdd) {
                updateEntityTagAdd(cmdEnv, cmd)
            }

            override fun addRelationshipTag(cmd: ModelCmd.UpdateRelationshipTagAdd) {
                updateRelationshipTagAdd(cmdEnv, cmd)
            }

            override fun addEntityAttributeTag(cmd: ModelCmd.UpdateEntityAttributeTagAdd) {
                updateEntityAttributeTagAdd(cmdEnv, cmd)
            }

            override fun addRelationshipAttributeTag(cmd: ModelCmd.UpdateRelationshipAttributeTagAdd) {
                updateRelationshipAttributeTagAdd(cmdEnv, cmd)
            }
        }
    }

    private fun createIdentityModelSourceDestIdMaps(): ModelSourceDestIdConv {
        return object : ModelSourceDestIdConv {
            override fun getDestEntityRef(sourceId: EntityId): EntityRef {
                return EntityRef.ById(sourceId)
            }

            override fun getDestRelationshipRef(sourceId: RelationshipId): RelationshipRef {
                return RelationshipRef.ById(sourceId)
            }

            override fun getDestEntityAttributeRef(sourceId: AttributeId): EntityAttributeRef {
                return EntityAttributeRef.ById(sourceId)
            }

            override fun getDestRelationshipAttributeRef(sourceId: AttributeId): RelationshipAttributeRef {
                return RelationshipAttributeRef.ById(sourceId)
            }
        }
    }

    private fun importModel(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.ImportModel) {

        // Makes sure that imported models are always with a SYSTEM authority
        val model = ModelAggregateInMemory.of(cmd.model).copy(
            model = ModelInMemory.of(cmd.model).copy(authority = ModelAuthority.SYSTEM)
        )

        if (storage.existsModelByKey(model.key)) throw ModelDuplicateKeyException(model.key)
        ensureImportedModelIsValid(model)
        storageDispatch(cmdEnv, StoreModelAggregatePayloadFactory.create(model))

        val newtags = cmd.tags

        // Register each found tag. At this point we don't know the tag id,
        // only the key. Only tags in the model scope are created with imports.
        // So, when we need to address a tag later, we will need to create
        // tag refs by key (and not ids)
        newtags.forEach { tag ->
            tagResolver.create(
                cmdEnv.traceabilityRecord,
                cmd.model.id,
                tag.key,
                tag.name,
                tag.description
            )
        }

        fun resolveTag(tagId: TagId): TagRef? {
            // As said before, we don't know the tag ids, only their keys in
            // the imported model scope. So we need to resolve the tagId from
            // the imported model aggregate to a ref by key. Hopefully, we have
            // the "ref" mechanism that avoids doing a lookup in the database
            // in this resolver.
            val tagKey = newtags.firstOrNull { it.id == tagId }?.key
            return if (tagKey != null) {
                tagRefKey(modelTagScopeRef(model.id), null, tagKey)
            } else null
        }

        // Read the model and temporary tag ids inside, then apply the tags to model elements

        ModelTaggerImpl().applyAllTags(
            source = model,
            destModelRef = ModelRef.ById(model.id),
            tagWriter = createTagWriter(cmdEnv),
            idMaps = createIdentityModelSourceDestIdMaps(),
            resolveTag = ::resolveTag
        )

        storageDispatch(cmdEnv, ModelStorageCmd.ModelRelease(model.id, model.version))
    }


    private fun deleteModel(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.DeleteModel) {
        val model = storage.findModel(cmd.modelRef)
        tagResolver.onModelDelete(cmdEnv.traceabilityRecord, model.id)
        storageDispatch(cmdEnv, ModelStorageCmd.DeleteModel(model.id))
    }

    private fun updateModelName(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateModelName) {
        val model = storage.findModel(cmd.modelRef)
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateModelName(model.id, cmd.name))
    }

    private fun updateModelKey(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateModelKey) {
        val model = storage.findModel(cmd.modelRef)
        if (model.key != cmd.key && storage.existsModelByKey(cmd.key)) {
            throw ModelDuplicateKeyException(cmd.key)
        }
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateModelKey(model.id, cmd.key))
    }

    private fun updateModelDescription(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateModelDescription) {
        val model = storage.findModel(cmd.modelRef)
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateModelDescription(model.id, cmd.description))
    }

    private fun updateModelAuthority(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateModelAuthority) {
        val model = storage.findModel(cmd.modelRef)
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateModelAuthority(model.id, cmd.authority))
    }

    private fun cmdModelRelease(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.ModelRelease) {
        val model = storage.findModel(cmd.modelRef)
        val previousVersion = storage.findLatestModelReleaseVersionOptional(model.id)
        if (previousVersion != null && cmd.version <= previousVersion) {
            throw ModelReleaseVersionMustBeGreaterThanPreviousException(cmd.modelRef, cmd.version, previousVersion)
        }
        storageDispatch(cmdEnv, ModelStorageCmd.ModelRelease(model.id, cmd.version))
    }

    private fun updateDocumentationHome(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateModelDocumentationHome) {
        val model = storage.findModel(cmd.modelRef)
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateModelDocumentationHome(model.id, cmd.url))
    }

    private fun updateModelTagAdd(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateModelTagAdd) {
        val model = storage.findModel(cmd.modelRef)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateModelTagAdd(
                model.id,
                tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateModelTagDelete(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateModelTagDelete) {
        val model = storage.findModel(cmd.modelRef)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateModelTagDelete(
                model.id,
                tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Types
    // -----------------------------------------------------------------------------------------------------------------


    private fun createType(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.CreateType) {
        // Cannot create a type if another type already has the same key in the model
        val model = storage.findModel(cmd.modelRef)
        val existing = storage.findTypeByKeyOptional(model.id, cmd.initializer.key)
        if (existing != null) throw TypeCreateDuplicateException(model.key, cmd.initializer.key)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.CreateType(
                modelId = model.id,
                typeId = TypeId.generate(),
                key = cmd.initializer.key,
                name = cmd.initializer.name,
                description = cmd.initializer.description
            )
        )
    }

    private fun updateTypeKey(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateTypeKey) {
        val (model, type) = findModelAndType(cmd.modelRef, cmd.typeRef)
        if (type.key == cmd.value) return
        val found = storage.findTypeByKeyOptional(model.id, cmd.value)
        if (found != null) throw TypeUpdateDuplicateKeyException(cmd.value)
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateTypeKey(model.id, type.id, cmd.value))
    }

    private fun updateTypeName(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateTypeName) {
        val (model, type) = findModelAndType(cmd.modelRef, cmd.typeRef)
        if (type.name == cmd.value) return
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateTypeName(model.id, type.id, cmd.value))
    }

    private fun updateTypeDescription(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateTypeDescription) {
        val (model, type) = findModelAndType(cmd.modelRef, cmd.typeRef)
        if (type.description == cmd.value) return
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateTypeDescription(model.id, type.id, cmd.value))
    }

    private fun deleteType(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.DeleteType) {
        val (model, type) = findModelAndType(cmd.modelRef, cmd.typeRef)

        val used = storage.isTypeUsedInEntityAttributes(model.id, type.id)
                || storage.isTypeUsedInRelationshipAttributes(model.id, type.id)

        if (used) throw ModelTypeDeleteUsedException(type.key)

        storageDispatch(cmdEnv, ModelStorageCmd.DeleteType(model.id, type.id))
    }
    // -----------------------------------------------------------------------------------------------------------------
    // Entities
    // -----------------------------------------------------------------------------------------------------------------


    private fun updateEntityKey(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityKey) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        val duplicate = storage.findEntityByKeyOptional(model.id, cmd.value)

        if (duplicate != null && duplicate.id != entity.id) {
            throw EntityUpdateKeyDuplicateKeyException(entity.key)
        }

        storageDispatch(cmdEnv, ModelStorageCmd.UpdateEntityKey(model.id, entity.id, cmd.value))
    }

    private fun updateEntityName(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityName) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        if (entity.name == cmd.value) return
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateEntityName(model.id, entity.id, cmd.value))
    }

    private fun updateEntityDescription(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityDescription) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        if (entity.description == cmd.value) return
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateEntityDescription(model.id, entity.id, cmd.value))
    }

    private fun updateEntityPrimaryKey(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityPrimaryKey) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        val attributeIds = cmd.attributeRefs.map { attributeRef ->
            storage.findEntityAttribute(model.id, entity.id, attributeRef).id
        }
        val currentPrimaryKey = storage.findEntityPrimaryKeyOptional(model.id, entity.id)
        val hasChanges = (attributeIds.isEmpty() && currentPrimaryKey != null)
                || (currentPrimaryKey != null && !currentPrimaryKey.containsInOrder(attributeIds))
        if (hasChanges) {
            storageDispatch(
                cmdEnv, ModelStorageCmd.Entity_PrimaryKey_Set(
                    modelId = model.id,
                    entityId = entity.id,
                    attributeIds = attributeIds
                )
            )
        }
    }

    private fun createBusinessKey(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.BusinessKeyCreate) {
        val model = storage.findModel(cmd.modelRef)
        val entity = storage.findEntity(model.id, cmd.entityRef)
        val participantAttributeIds = cmd.participants.map { participantRef ->
            storage.findEntityAttribute(model.id, entity.id, participantRef).id
        }
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.BusinessKeyCreate(
                modelId = model.id,
                entityId = entity.id,
                businessKeyId = BusinessKeyId.generate(),
                key = cmd.key,
                name = cmd.name,
                description = cmd.description,
                participantAttributeIds = participantAttributeIds
            )
        )
    }

    private fun updateEntityDocumentationHome(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityDocumentationHome) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        if (entity.documentationHome?.toExternalForm() == cmd.value?.toExternalForm()) return
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateEntityDocumentationHome(model.id, entity.id, cmd.value))
    }

    private fun updateEntityTagAdd(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityTagAdd) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateEntityTagAdd(
                model.id, entity.id,
                tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateEntityTagDelete(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityTagDelete) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateEntityTagDelete(
                model.id, entity.id,
                tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }


    private fun createEntity(cmdEnv: ModelCmdEnveloppe, c: ModelCmd.CreateEntity) {
        val model = storage.findModel(c.modelRef)
        val type = storage.findType(model.id, c.entityInitializer.identityAttribute.type)
        val identityAttributeId = AttributeId.generate()
        val entityId = EntityId.generate()

        storageDispatch(
            cmdEnv, ModelStorageCmd.CreateEntity(
                modelId = model.id,
                entityId = entityId,
                key = c.entityInitializer.entityKey,
                name = c.entityInitializer.name,
                description = c.entityInitializer.description,
                documentationHome = c.entityInitializer.documentationHome,
                origin = EntityOrigin.Manual
            )
        )
        storageDispatch(
            cmdEnv, ModelStorageCmd.CreateEntityAttribute(
                modelId = model.id,
                entityId = entityId,
                attributeId = identityAttributeId,
                key = c.entityInitializer.identityAttribute.attributeKey,
                name = c.entityInitializer.identityAttribute.name,
                description = c.entityInitializer.identityAttribute.description,
                typeId = type.id,
                optional = false // because it's identity, can never be optional
            )
        )
        storageDispatch(
            cmdEnv, ModelStorageCmd.Entity_PrimaryKey_Set(
                modelId = model.id,
                entityId = entityId,
                attributeIds = listOf(identityAttributeId)
            )
        )

    }

    private fun deleteEntity(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.DeleteEntity) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        storageDispatch(cmdEnv, ModelStorageCmd.DeleteEntity(model.id, entity.id))
    }

    private fun createEntityAttribute(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.CreateEntityAttribute) {
        val (model, entity) = findModelAndEntity(cmd.modelRef, cmd.entityRef)
        val found = storage.findEntityAttributeByKeyOptional(model.id, entity.id, cmd.attributeInitializer.attributeKey)
        if (found != null)
            throw CreateAttributeDuplicateKeyException(entity.key, cmd.attributeInitializer.attributeKey)

        // Makes sure the type reference exists, even if now, the type is referenced by a key only
        val typeRef = cmd.attributeInitializer.type
        val type = storage.findType(model.id, typeRef)

        storageDispatch(
            cmdEnv,
            ModelStorageCmd.CreateEntityAttribute(
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

    private fun deleteEntityAttribute(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.DeleteEntityAttribute) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.DeleteEntityAttribute(
                modelId = model.id,
                entityId = entity.id,
                attributeId = attribute.id
            )
        )
    }

    private fun updateEntityAttributeKey(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityAttributeKey) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        val found = storage.findEntityAttributeByKeyOptional(model.id, entity.id, cmd.value)
        if (found != null && found.id != attribute.id) throw UpdateAttributeDuplicateKeyException(
            cmd.entityRef,
            cmd.attributeRef,
            cmd.value
        )
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateEntityAttributeKey(model.id, entity.id, attribute.id, cmd.value)
        )
    }

    private fun updateEntityAttributeName(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityAttributeName) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (attribute.name == cmd.value) return
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateEntityAttributeName(model.id, entity.id, attribute.id, cmd.value))
    }

    private fun updateEntityAttributeDescription(
        cmdEnv: ModelCmdEnveloppe,
        cmd: ModelCmd.UpdateEntityAttributeDescription
    ) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (attribute.description == cmd.value) return
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateEntityAttributeDescription(model.id, entity.id, attribute.id, cmd.value)
        )
    }

    private fun updateEntityAttributeType(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityAttributeType) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        val type = storage.findType(model.id, cmd.value)
        if (attribute.typeId == type.id) return
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateEntityAttributeType(model.id, entity.id, attribute.id, type.id))
    }

    private fun updateEntityAttributeOptional(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityAttributeOptional) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        if (attribute.optional == cmd.value) return
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateEntityAttributeOptional(model.id, entity.id, attribute.id, cmd.value)
        )
    }

    private fun updateEntityAttributeTagAdd(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateEntityAttributeTagAdd) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateEntityAttributeTagAdd(
                modelId = model.id,
                entityId = entity.id,
                attributeId = attribute.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateEntityAttributeTagDelete(
        cmdEnv: ModelCmdEnveloppe,
        cmd: ModelCmd.UpdateEntityAttributeTagDelete
    ) {
        val (model, entity, attribute) = findModelAndEntityAndAttribute(cmd.modelRef, cmd.entityRef, cmd.attributeRef)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateEntityAttributeTagDelete(
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


    private fun deleteRelationshipAttribute(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.DeleteRelationshipAttribute) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.DeleteRelationshipAttribute(
                modelId = model.id,
                relationshipId = relationship.id,
                attributeId = attribute.id
            )
        )
    }

    private fun updateRelationshipAttributeKey(
        cmdEnv: ModelCmdEnveloppe,
        cmd: ModelCmd.UpdateRelationshipAttributeKey
    ) {
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
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipAttributeKey(model.id, relationship.id, attribute.id, cmd.value)
        )
    }

    private fun updateRelationshipAttributeName(
        cmdEnv: ModelCmdEnveloppe,
        cmd: ModelCmd.UpdateRelationshipAttributeName
    ) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        if (attribute.name == cmd.value) return
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipAttributeName(
                model.id,
                relationship.id,
                attribute.id,
                cmd.value
            )
        )
    }

    private fun updateRelationshipAttributeDescription(
        cmdEnv: ModelCmdEnveloppe,
        cmd: ModelCmd.UpdateRelationshipAttributeDescription
    ) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        if (attribute.description == cmd.value) return
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipAttributeDescription(
                model.id,
                relationship.id,
                attribute.id,
                cmd.value
            )
        )
    }

    private fun updateRelationshipAttributeType(
        cmdEnv: ModelCmdEnveloppe,
        cmd: ModelCmd.UpdateRelationshipAttributeType
    ) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        val type = storage.findType(model.id, cmd.value)
        if (attribute.typeId == type.id) return
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipAttributeType(model.id, relationship.id, attribute.id, type.id)
        )
    }

    private fun updateRelationshipAttributeOptional(
        cmdEnv: ModelCmdEnveloppe,
        cmd: ModelCmd.UpdateRelationshipAttributeOptional
    ) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        if (attribute.optional == cmd.value) return
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipAttributeOptional(
                model.id,
                relationship.id,
                attribute.id,
                cmd.value
            )
        )
    }

    private fun updateRelationshipAttributeTagAdd(
        cmdEnv: ModelCmdEnveloppe,
        cmd: ModelCmd.UpdateRelationshipAttributeTagAdd
    ) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipAttributeTagAdd(
                modelId = model.id,
                relationshipId = relationship.id,
                attributeId = attribute.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateRelationshipAttributeTagDelete(
        cmdEnv: ModelCmdEnveloppe,
        cmd: ModelCmd.UpdateRelationshipAttributeTagDelete
    ) {
        val (model, relationship, attribute) = findModelAndRelationshipAndAttribute(
            cmd.modelRef,
            cmd.relationshipRef,
            cmd.attributeRef
        )
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipAttributeTagDelete(
                modelId = model.id,
                relationshipId = relationship.id,
                attributeId = attribute.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun deleteRelationship(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.DeleteRelationship) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.DeleteRelationship(
                modelId = model.id,
                relationshipId = relationship.id,
            )
        )
    }

    private fun updateRelationshipKey(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateRelationshipKey) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        if (relationship.key == cmd.value) return
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateRelationshipKey(model.id, relationship.id, cmd.value))
    }

    private fun updateRelationshipName(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateRelationshipName) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        if (relationship.name == cmd.value) return
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateRelationshipName(model.id, relationship.id, cmd.value))
    }

    private fun updateRelationshipDescription(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateRelationshipDescription) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        if (relationship.description == cmd.value) return
        storageDispatch(cmdEnv, ModelStorageCmd.UpdateRelationshipDescription(model.id, relationship.id, cmd.value))
    }

    private fun createRelationshipRole(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.CreateRelationshipRole) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        val duplicate = storage.findRelationshipRoleByKeyOptional(model.id, relationship.id, cmd.key)
        if (duplicate != null) {
            throw RelationshipRoleCreateDuplicateKeyException(cmd.modelRef, cmd.relationshipRef, cmd.key)
        }
        val entity = storage.findEntity(model.id, cmd.entityRef)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.CreateRelationshipRole(
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

    private fun updateRelationshipRoleKey(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateRelationshipRoleKey) {
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
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipRoleKey(model.id, relationship.id, role.id, cmd.value)
        )
    }

    private fun updateRelationshipRoleName(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateRelationshipRoleName) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        val role = storage.findRelationshipRole(model.id, relationship.id, cmd.relationshipRoleRef)
        if (role.name == cmd.value) return
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipRoleName(model.id, relationship.id, role.id, cmd.value)
        )
    }

    private fun updateRelationshipRoleEntity(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateRelationshipRoleEntity) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        val role = storage.findRelationshipRole(model.id, relationship.id, cmd.relationshipRoleRef)
        val entity = storage.findEntity(model.id, cmd.value)
        if (role.entityId == entity.id) return
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipRoleEntity(model.id, relationship.id, role.id, entity.id)
        )
    }

    private fun updateRelationshipRoleCardinality(
        cmdEnv: ModelCmdEnveloppe,
        cmd: ModelCmd.UpdateRelationshipRoleCardinality
    ) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        val role = storage.findRelationshipRole(model.id, relationship.id, cmd.relationshipRoleRef)
        if (role.cardinality == cmd.value) return
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipRoleCardinality(model.id, relationship.id, role.id, cmd.value)
        )
    }

    private fun deleteRelationshipRole(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.DeleteRelationshipRole) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        if (relationship.roles.size <= 2) {
            throw RelationshipRoleDeleteMinimumRolesException(cmd.modelRef, cmd.relationshipRef)
        }
        val role = storage.findRelationshipRole(model.id, relationship.id, cmd.relationshipRoleRef)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.DeleteRelationshipRole(
                modelId = model.id,
                relationshipId = relationship.id,
                relationshipRoleId = role.id
            )
        )
    }

    private fun updateRelationshipTagAdd(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateRelationshipTagAdd) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipTagAdd(
                modelId = model.id,
                relationshipId = relationship.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun updateRelationshipTagDelete(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.UpdateRelationshipTagDelete) {
        val (model, relationship) = findModelAndRelationship(cmd.modelRef, cmd.relationshipRef)
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.UpdateRelationshipTagDelete(
                modelId = model.id,
                relationshipId = relationship.id,
                tagId = tagResolver.resolveTagIdCompatible(model.id, cmd.tagRef)
            )
        )
    }

    private fun createRelationshipAttribute(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.CreateRelationshipAttribute) {
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
        storageDispatch(
            cmdEnv,
            ModelStorageCmd.CreateRelationshipAttribute(
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


    private fun createRelationship(cmdEnv: ModelCmdEnveloppe, cmd: ModelCmd.CreateRelationship) {
        val model = storage.findModel(cmd.modelRef)
        val duplicateKey = storage.findRelationshipByKeyOptional(model.id, cmd.initializer.key)

        if (duplicateKey != null)
            throw RelationshipDuplicateIdException(model.id, cmd.initializer.key)

        val duplicateRoleIds =
            cmd.initializer.roles.groupBy { it.key }.mapValues { it.value.size }.filter { it.value > 1 }
        if (duplicateRoleIds.isNotEmpty()) {
            throw RelationshipDuplicateRoleIdException(duplicateRoleIds.keys)
        }

        storageDispatch(
            cmdEnv,
            ModelStorageCmd.CreateRelationship(
                modelId = model.id,
                relationshipId = RelationshipId.generate(),
                name = cmd.initializer.name,
                description = cmd.initializer.description,
                key = cmd.initializer.key,
                roles = cmd.initializer.roles.map {
                    val entity = storage.findEntity(model.id, it.entityRef)
                    ModelStorageCmd.RelationshipRoleInitializer(
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
        val exists = storage.existsModel(modelRef)
        if (!exists) throw ModelNotFoundException(modelRef)
    }

    private fun ensureImportedModelIsValid(model: ModelAggregate) {
        when (val validation = modelValidation.validate(model)) {
            is ModelValidationState.Ok -> return
            is ModelValidationState.Error -> throw ModelInvalidException(model.id, validation.errors)
        }
    }


}
