package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.tables.EntityPKTable
import org.jetbrains.exposed.v1.core.ResultRow

data class EntityPKRecord(
    val snapshotId: EntityPKSnapshotId,
    val lineageId: EntityPrimaryKeyId,
    val modelEntitySnapshotId: EntitySnapshotId
) {
    companion object {
        fun read(row: ResultRow): EntityPKRecord {
            return EntityPKRecord(
                snapshotId = row[EntityPKTable.id],
                lineageId = row[EntityPKTable.lineageId],
                modelEntitySnapshotId = row[EntityPKTable.entitySnapshotId]
            )
        }
    }
}
