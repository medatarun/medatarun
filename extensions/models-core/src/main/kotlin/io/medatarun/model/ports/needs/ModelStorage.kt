package io.medatarun.model.ports.needs

import io.medatarun.model.domain.*
import io.medatarun.model.domain.search.SearchResults
import io.medatarun.tags.core.domain.TagId

/**
 * Model storage allows accessing and modifying stored models.
 *
 * The current runtime uses one native SQL storage selected by the model extension itself.
 */
interface ModelStorage {

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    // Model

    fun existsModelById(id: ModelId): Boolean

    fun existsModelByKey(key: ModelKey): Boolean

    fun existsModel(ref: ModelRef): Boolean {
        return when (ref) {
            is ModelRef.ById -> existsModelById(ref.id)
            is ModelRef.ByKey -> existsModelByKey(ref.key)
        }
    }

    fun findAllModelIds(): List<ModelId>

    fun findModelByKeyOptional(key: ModelKey): Model?

    fun findModelByIdOptional(id: ModelId): Model?

    fun findModelOptional(ref: ModelRef): Model? {
        return when (ref) {
            is ModelRef.ByKey -> findModelByKeyOptional(ref.key)
            is ModelRef.ById -> findModelByIdOptional(ref.id)
        }
    }

    fun findModel(ref: ModelRef): Model {
        return findModelOptional(ref) ?: throw ModelNotFoundException(ref)
    }

    fun findModelTags(modelId: ModelId): List<TagId>

    fun findModelAggregateVersionOptional(modelId: ModelId, modelVersion: ModelVersion): ModelAggregate?

    fun findModelAggregateVersion(modelId: ModelId, modelVersion: ModelVersion): ModelAggregate =
        findModelAggregateVersionOptional(modelId, modelVersion) ?: throw ModelNotFoundException(
            ModelRef.modelRefId(
                modelId
            )
        )

    fun findLatestModelReleaseVersionOptional(modelId: ModelId): ModelVersion?

    // Model aggregate

    fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate?

    fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate?

    fun findModelAggregateOptional(ref: ModelRef): ModelAggregate? = when (ref) {
        is ModelRef.ByKey -> findModelAggregateByKeyOptional(ref.key)
        is ModelRef.ById -> findModelAggregateByIdOptional(ref.id)
    }

    fun findModelAggregate(ref: ModelRef): ModelAggregate {
        return findModelAggregateOptional(ref) ?: throw ModelNotFoundException(ref)
    }

    // Type

    fun findTypeByIdOptional(modelId: ModelId, typeId: TypeId): ModelType?

    fun findTypeByKeyOptional(modelId: ModelId, key: TypeKey): ModelType?

    fun findTypeOptional(modelId: ModelId, typeRef: TypeRef): ModelType? {
        return when (typeRef) {
            is TypeRef.ById -> findTypeByIdOptional(modelId, typeRef.id)
            is TypeRef.ByKey -> findTypeByKeyOptional(modelId, typeRef.key)
        }
    }

    fun findType(modelId: ModelId, typeRef: TypeRef): ModelType {
        val type = findTypeOptional(modelId, typeRef)
            ?: throw TypeNotFoundException(ModelRef.ById(modelId), typeRef)
        return type
    }

    fun findTypes(modelId: ModelId): List<ModelType>

    // Entity

    fun findEntityByIdOptional(modelId: ModelId, entityId: EntityId): Entity?

    fun findEntityByKeyOptional(modelId: ModelId, entityKey: EntityKey): Entity?

    fun findEntityOptional(modelId: ModelId, entityRef: EntityRef): Entity? {
        return when (entityRef) {
            is EntityRef.ById -> findEntityByIdOptional(modelId, entityRef.id)
            is EntityRef.ByKey -> findEntityByKeyOptional(modelId, entityRef.key)
        }
    }

    fun findEntity(modelId: ModelId, entityRef: EntityRef): Entity {
        return findEntityOptional(modelId, entityRef) ?: throw EntityNotFoundException(
            ModelRef.ById(modelId),
            entityRef
        )
    }

    fun findEntityPrimaryKeyOptional(modelId: ModelId, entityId: EntityId): EntityPrimaryKey?

    // Business key

    fun findBusinessKeyByIdOptional(modelId: ModelId, id: BusinessKeyId): BusinessKey?

    fun findBusinessKeyByKeyOptional(modelId: ModelId, key: BusinessKeyKey): BusinessKey?

    fun findBusinessKeys(modelId: ModelId): List<BusinessKey>

    fun findBusinessKeyOptional(modelId: ModelId, ref: BusinessKeyRef): BusinessKey? {
        return when (ref) {
            is BusinessKeyRef.ById -> findBusinessKeyByIdOptional(modelId, ref.id)
            is BusinessKeyRef.ByKey -> findBusinessKeyByKeyOptional(modelId, ref.key)
        }
    }

    // Entity attribute

    fun findEntityAttributeByIdOptional(modelId: ModelId, entityId: EntityId, attributeId: AttributeId): Attribute?

    fun findEntityAttributeByKeyOptional(modelId: ModelId, entityId: EntityId, key: AttributeKey): Attribute?

    fun findEntityAttributeOptional(
        modelId: ModelId,
        entityid: EntityId,
        attributeRef: EntityAttributeRef
    ): Attribute? {
        return when (attributeRef) {
            is EntityAttributeRef.ById -> findEntityAttributeByIdOptional(modelId, entityid, attributeRef.id)
            is EntityAttributeRef.ByKey -> findEntityAttributeByKeyOptional(modelId, entityid, attributeRef.key)
        }
    }

    fun findEntityAttribute(modelId: ModelId, entityid: EntityId, attributeRef: EntityAttributeRef): Attribute {
        return findEntityAttributeOptional(modelId, entityid, attributeRef)
            ?: throw EntityAttributeNotFoundException(ModelRef.ById(modelId), EntityRef.ById(entityid), attributeRef)
    }

    // Relationships

    fun findRelationshipList(modelId: ModelId): List<Relationship>

    fun findRelationshipByIdOptional(modelId: ModelId, relationshipId: RelationshipId): Relationship?

    fun findRelationshipByKeyOptional(modelId: ModelId, relationshipKey: RelationshipKey): Relationship?

    fun findRelationshipOptional(modelId: ModelId, relationshipRef: RelationshipRef): Relationship? {
        return when (relationshipRef) {
            is RelationshipRef.ById -> findRelationshipByIdOptional(modelId, relationshipRef.id)
            is RelationshipRef.ByKey -> findRelationshipByKeyOptional(modelId, relationshipRef.key)
        }
    }

    fun findRelationship(modelId: ModelId, relationshipRef: RelationshipRef): Relationship {
        return findRelationshipOptional(modelId, relationshipRef)
            ?: throw RelationshipNotFoundException(ModelRef.ById(modelId), relationshipRef)
    }

    // Relationship role

    fun findRelationshipRoleByIdOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        roleId: RelationshipRoleId
    ): RelationshipRole?

    fun findRelationshipRoleByKeyOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        roleKey: RelationshipRoleKey
    ): RelationshipRole?

    fun findRelationshipRoleOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        roleRef: RelationshipRoleRef
    ): RelationshipRole? {
        return when (roleRef) {
            is RelationshipRoleRef.ById -> findRelationshipRoleByIdOptional(modelId, relationshipId, roleRef.id)
            is RelationshipRoleRef.ByKey -> findRelationshipRoleByKeyOptional(modelId, relationshipId, roleRef.key)
        }
    }

    fun findRelationshipRole(
        modelId: ModelId,
        relationshipId: RelationshipId,
        roleRef: RelationshipRoleRef
    ): RelationshipRole {
        return findRelationshipRoleOptional(modelId, relationshipId, roleRef)
            ?: throw RelationshipRoleNotFoundException(
                ModelRef.ById(modelId),
                RelationshipRef.ById(relationshipId),
                roleRef
            )
    }

    // Relationship attribute

    fun findRelationshipAttributeByIdOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        attributeId: AttributeId
    ): Attribute?

    fun findRelationshipAttributeByKeyOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        key: AttributeKey
    ): Attribute?

    fun findRelationshipAttributeOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        attributeRef: RelationshipAttributeRef
    ): Attribute? {
        return when (attributeRef) {
            is RelationshipAttributeRef.ById -> findRelationshipAttributeByIdOptional(
                modelId,
                relationshipId,
                attributeRef.id
            )

            is RelationshipAttributeRef.ByKey -> findRelationshipAttributeByKeyOptional(
                modelId,
                relationshipId,
                attributeRef.key
            )
        }
    }

    fun findRelationshipAttribute(
        modelId: ModelId,
        relationshipId: RelationshipId,
        attributeRef: RelationshipAttributeRef
    ): Attribute {
        return findRelationshipAttributeOptional(modelId, relationshipId, attributeRef)
            ?: throw RelationshipAttributeNotFoundException(
                ModelRef.ById(modelId),
                RelationshipRef.ById(relationshipId),
                attributeRef
            )
    }

    fun findDomainTagLocationsByTagId(tagId: TagId): List<DomainTagLocation>

    // -------------------------------------------------------------------------
    // History
    // -------------------------------------------------------------------------

    fun findModelVersions(modelId: ModelId): List<ModelChangeEvent>
    fun findAllModelChangeEvent(modelId: ModelId): List<ModelChangeEvent>
    fun findModelChangeEventsInVersion(modelId: ModelId, version: ModelVersion): List<ModelChangeEvent>
    fun findModelChangeEventsSinceLastReleaseEvent(modelId: ModelId): List<ModelChangeEvent>

    /**
     * Finds the latest known model change event
     */
    fun findLastModelChangeEventOptional(modelId: ModelId): ModelChangeEvent?
    fun findLastModelChangeEvent(modelId: ModelId): ModelChangeEvent = findLastModelChangeEventOptional(modelId)
        ?: throw ModelNotFoundByIdException(modelId)

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    fun search(query: ModelStorageSearchQuery): SearchResults

    // -------------------------------------------------------------------------
    // Analytics
    // -------------------------------------------------------------------------

    fun isTypeUsedInEntityAttributes(modelId: ModelId, typeId: TypeId): Boolean
    fun isTypeUsedInRelationshipAttributes(modelId: ModelId, typeId: TypeId): Boolean

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    /**
     * Process one model storage command.
     *
     * See [ModelStorageCmd] for the list of supported write operations.
     */
    fun dispatch(cmdEnv: ModelStorageCmdEnveloppe)

    /**
     * Rebuilds all model projection tables by replaying persisted events.
     *
     * This operation is for maintenance only and is expected to be used only when
     * projection data is suspected to be out of sync with event storage.
     */
    fun maintenanceRebuildCaches()


}
