package io.medatarun.model.infra.db.aggregate

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.*
import io.medatarun.model.infra.db.tables.*
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select

class ModelStorageDbSnapshots {

    val CURRENT_HEAD_SNAPSHOT_KIND = "CURRENT_HEAD"
    val VERSION_SNAPSHOT_KIND = "VERSION_SNAPSHOT"

    fun currentHeadModelSnapshotId(modelId: ModelId): ModelSnapshotId {
        return findCurrentHeadModelSnapshotId(modelId) ?: throw ModelStorageDbMissingCurrentHeadModelSnapshotException(
            modelId
        )
    }
    fun currentHeadTypeSnapshotId(modelId: ModelId, typeId: TypeId): TypeSnapshotId {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        return currentHeadTypeSnapshotIdInModelSnapshot(modelSnapshotId, typeId)
    }

    fun currentHeadTypeSnapshotIdInModelSnapshot(modelSnapshotId: ModelSnapshotId, typeId: TypeId): TypeSnapshotId {
        val row = ModelTypeTable.select(ModelTypeTable.id).where {
            (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
        }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingTypeSnapshotException(typeId)
        }
        return row[ModelTypeTable.id]
    }

    fun currentHeadEntitySnapshotIdInModelSnapshot(modelSnapshotId: ModelSnapshotId, entityId: EntityId): EntitySnapshotId {
        val row = EntityTable.select(EntityTable.id).where {
            (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
        }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingEntitySnapshotException(entityId)
        }
        return row[EntityTable.id]
    }

    fun currentHeadAttributeSnapshotIdInModelSnapshot(modelSnapshotId: ModelSnapshotId, attributeId: AttributeId): AttributeSnapshotId {
        val entityAttributeRow = EntityAttributeTable.join(
            EntityTable,
            JoinType.INNER,
            EntityAttributeTable.entitySnapshotId,
            EntityTable.id
        ).select(EntityAttributeTable.id)
            .where {
                (EntityAttributeTable.lineageId eq attributeId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }
            .singleOrNull()
        if (entityAttributeRow != null) {
            return entityAttributeRow[EntityAttributeTable.id]
        }
        val relationshipAttributeRow = RelationshipAttributeTable.join(
            RelationshipTable,
            JoinType.INNER,
            RelationshipAttributeTable.relationshipSnapshotId,
            RelationshipTable.id
        ).select(RelationshipAttributeTable.id)
            .where {
                (RelationshipAttributeTable.lineageId eq attributeId) and
                        (RelationshipTable.modelSnapshotId eq modelSnapshotId)
            }
            .singleOrNull()
        if (relationshipAttributeRow != null) {
            return relationshipAttributeRow[RelationshipAttributeTable.id]
        }
        throw ModelStorageDbMissingAttributeSnapshotException(attributeId)
    }


    fun currentHeadRelationshipSnapshotIdInModelSnapshot(modelSnapshotId: ModelSnapshotId, relationshipId: RelationshipId): RelationshipSnapshotId {
        val row = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq relationshipId) and (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingRelationshipSnapshotException(relationshipId)
        }
        return row[RelationshipTable.id]
    }

    private fun findCurrentHeadModelSnapshotId(modelId: ModelId): ModelSnapshotId? {
        val row = ModelSnapshotTable.select(ModelSnapshotTable.id).where {
            currentHeadModelSnapshotCriteria(modelId)
        }.singleOrNull()
        if (row == null) {
            return null
        }
        return row[ModelSnapshotTable.id]
    }

    /**
     * Mutable model-level state lives only on the current head projection.
     * Version snapshots must stay immutable once they exist.
     */
    fun currentHeadModelSnapshotCriteria(modelId: ModelId): Op<Boolean> {
        return (ModelSnapshotTable.modelId eq modelId) and
                (ModelSnapshotTable.snapshotKind eq CURRENT_HEAD_SNAPSHOT_KIND)
    }

}
