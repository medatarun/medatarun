package io.medatarun.model.infra.db

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntitySnapshotId
import io.medatarun.model.infra.db.tables.EntityAttributeTable
import io.medatarun.model.infra.db.tables.EntityPKAttributeTable
import io.medatarun.model.infra.db.tables.EntityPKTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll

internal object ModelStorageDbCompatibilityReads {

    /**
     * During compatibility phase, derive Entity.identifierAttributeId from PK participants
     * instead of the legacy identifier column.
     */
    fun findIdentifierAttributeIdFromPrimaryKey(
        entitySnapshotId: EntitySnapshotId,
        entityId: EntityId
    ): AttributeId {
        val row = EntityPKTable.join(
            EntityPKAttributeTable,
            JoinType.INNER,
            onColumn = EntityPKTable.id,
            otherColumn = EntityPKAttributeTable.entityPKSnapshotId
        ).join(
            EntityAttributeTable,
            JoinType.INNER,
            onColumn = EntityPKAttributeTable.attributeSnapshotId,
            otherColumn = EntityAttributeTable.id
        ).selectAll().where {
            (EntityPKTable.entitySnapshotId eq entitySnapshotId) and
                    (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
        }.orderBy(
            EntityPKAttributeTable.priority to SortOrder.ASC,
            EntityPKTable.id to SortOrder.ASC
        ).limit(1).singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingCompatibilityIdentifierPrimaryKeyException(entityId)
        }
        return row[EntityAttributeTable.lineageId]
    }
}
