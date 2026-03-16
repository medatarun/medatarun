package io.medatarun.model.infra.db.snapshots

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeSnapshotId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntitySnapshotId
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelSnapshotKind
import io.medatarun.model.domain.ModelSnapshotId
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipSnapshotId
import io.medatarun.model.domain.TypeId
import io.medatarun.model.domain.TypeSnapshotId
import io.medatarun.model.infra.db.ModelStorageDbMissingAttributeSnapshotException
import io.medatarun.model.infra.db.ModelStorageDbMissingCurrentHeadModelSnapshotException
import io.medatarun.model.infra.db.ModelStorageDbMissingEntitySnapshotException
import io.medatarun.model.infra.db.ModelStorageDbMissingRelationshipSnapshotException
import io.medatarun.model.infra.db.ModelStorageDbMissingTypeSnapshotException
import io.medatarun.model.infra.db.tables.EntityAttributeTable
import io.medatarun.model.infra.db.tables.EntityTable
import io.medatarun.model.infra.db.tables.ModelSnapshotTable
import io.medatarun.model.infra.db.tables.ModelTypeTable
import io.medatarun.model.infra.db.tables.RelationshipAttributeTable
import io.medatarun.model.infra.db.tables.RelationshipTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select

class ModelStorageDbSnapshots {

    fun currentHeadModelSnapshotId(modelId: ModelId): ModelSnapshotId {
        return findCurrentHeadModelSnapshotId(modelId) ?: throw ModelStorageDbMissingCurrentHeadModelSnapshotException(
            modelId
        )
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
            SnapshotSelector.CurrentHeadByModelId(modelId).criterion()
        }.singleOrNull()
        if (row == null) {
            return null
        }
        return row[ModelSnapshotTable.id]
    }

}
