package io.medatarun.model.infra.db.snapshots

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.*
import io.medatarun.model.infra.db.tables.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select

class ModelStorageDbSnapshots {

    fun currentHeadModelSnapshotId(modelId: ModelId): ModelSnapshotId {
        return currentHeadModelSnapshotIdOptional(modelId) ?: throw ModelStorageDbMissingCurrentHeadModelSnapshotException(
            modelId
        )
    }

    private fun currentHeadModelSnapshotIdOptional(modelId: ModelId): ModelSnapshotId? {
        val row = ModelSnapshotTable.select(ModelSnapshotTable.id).where {
            SnapshotSelector.CurrentHeadByModelId(modelId).criterion()
        }.singleOrNull()
        if (row == null) {
            return null
        }
        return row[ModelSnapshotTable.id]
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

    fun currentHeadEntitySnapshotIdInModelSnapshot(
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

    fun currentHeadRelationshipSnapshotIdInModelSnapshot(
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


}
