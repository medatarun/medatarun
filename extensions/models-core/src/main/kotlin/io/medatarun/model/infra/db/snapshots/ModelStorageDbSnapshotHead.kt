package io.medatarun.model.infra.db.snapshots

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.*
import io.medatarun.model.infra.db.tables.*
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select

class ModelStorageDbSnapshotHead {
    data class BusinessKeyAndEntitySnapshotId(
        val businessKeySnapshotId: BusinessKeySnapshotId,
        val entitySnapshotId: EntitySnapshotId
    )

    fun toModelSnapshotId(modelId: ModelId): ModelSnapshotId {
        return toModelSnapshotIdOptional(modelId) ?: throw ModelStorageDbMissingCurrentHeadModelSnapshotException(
            modelId
        )
    }

    private fun toModelSnapshotIdOptional(modelId: ModelId): ModelSnapshotId? {
        val row = ModelSnapshotTable.select(ModelSnapshotTable.id).where {
            SnapshotSelector.CurrentHeadByModelId(modelId).criterion()
        }.singleOrNull()
        if (row == null) {
            return null
        }
        return row[ModelSnapshotTable.id]
    }

    fun toTypeSnapshotId(modelSnapshotId: ModelSnapshotId, typeId: TypeId): TypeSnapshotId {
        val row = ModelTypeTable.select(ModelTypeTable.id).where {
            (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
        }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingTypeSnapshotException(typeId)
        }
        return row[ModelTypeTable.id]
    }

    fun toEntitySnapshotId(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId
    ): EntitySnapshotId {
        val row = EntityTable.select(EntityTable.id).where {
            (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
        }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingEntitySnapshotException(entityId)
        }
        return row[EntityTable.id]
    }

    fun toEntityAttributeSnapshotIds(
        entitySnapshotId: EntitySnapshotId,
        attributeIds: List<AttributeId>
    ): List<AttributeSnapshotId> {
        return attributeIds.map { attributeId ->
            val row = EntityAttributeTable.select(EntityAttributeTable.id).where {
                (EntityAttributeTable.entitySnapshotId eq entitySnapshotId) and
                        (EntityAttributeTable.lineageId eq attributeId)
            }.singleOrNull()
            if (row == null) {
                throw ModelStorageDbMissingAttributeSnapshotException(attributeId)
            }
            row[EntityAttributeTable.id]
        }
    }

    fun toRelationshipSnapshotId(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId
    ): RelationshipSnapshotId {
        val row = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq relationshipId) and (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingRelationshipSnapshotException(relationshipId)
        }
        return row[RelationshipTable.id]
    }

    fun toBusinessKeySnapshotId(
        modelSnapshotId: ModelSnapshotId,
        businessKeyId: BusinessKeyId
    ): BusinessKeySnapshotId {
        val row = BusinessKeyTable.join(
            EntityTable,
            JoinType.INNER,
            onColumn = BusinessKeyTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).select(BusinessKeyTable.id).where {
            (BusinessKeyTable.lineageId eq businessKeyId) and
                    (EntityTable.modelSnapshotId eq modelSnapshotId)
        }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingBusinessKeySnapshotException(businessKeyId)
        }
        return row[BusinessKeyTable.id]
    }

    /**
     * Returns the businessKeySnapshotId and the owning entitySnapshotId
     * in only one query (join).
     * This is used when replacing business key participants:
     * - remove existing participants with businessKeySnapshotId
     * - resolve new attribute ids in the correct entity with entitySnapshotId
     */
    fun toBusinessKeyAndEntitySnapshotId(
        modelSnapshotId: ModelSnapshotId,
        businessKeyId: BusinessKeyId
    ): BusinessKeyAndEntitySnapshotId {
        val row = BusinessKeyTable.join(
            EntityTable,
            JoinType.INNER,
            onColumn = BusinessKeyTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).select(BusinessKeyTable.id, BusinessKeyTable.entitySnapshotId).where {
            (BusinessKeyTable.lineageId eq businessKeyId) and
                    (EntityTable.modelSnapshotId eq modelSnapshotId)
        }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingBusinessKeySnapshotException(businessKeyId)
        }
        return BusinessKeyAndEntitySnapshotId(
            businessKeySnapshotId = row[BusinessKeyTable.id],
            entitySnapshotId = row[BusinessKeyTable.entitySnapshotId]
        )
    }


}
