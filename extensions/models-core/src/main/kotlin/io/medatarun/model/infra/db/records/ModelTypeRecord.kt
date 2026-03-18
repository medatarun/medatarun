package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.tables.ModelTypeTable
import org.jetbrains.exposed.v1.core.ResultRow

data class ModelTypeRecord(
    val snapshotId: TypeSnapshotId,
    val lineageId: TypeId,
    val modelSnapshotId: ModelSnapshotId,
    val key: TypeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
) {
    companion object {
        fun read(row: ResultRow): ModelTypeRecord {
            return ModelTypeRecord(
                snapshotId = row[ModelTypeTable.id],
                lineageId = row[ModelTypeTable.lineageId],
                modelSnapshotId = row[ModelTypeTable.modelSnapshotId],
                key = row[ModelTypeTable.key],
                name = row[ModelTypeTable.name],
                description = row[ModelTypeTable.description]
            )
        }
    }
}
