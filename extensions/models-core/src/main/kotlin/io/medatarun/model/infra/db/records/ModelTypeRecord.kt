package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.TypeId
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.infra.db.tables.ModelTypeTable
import org.jetbrains.exposed.v1.core.ResultRow

data class ModelTypeRecord(
    val snapshotId: TypeId,
    val lineageId: TypeId,
    val modelSnapshotId: ModelId,
    val key: TypeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
) {
    companion object {
        fun read(row: ResultRow): ModelTypeRecord {
            return ModelTypeRecord(
                snapshotId = row[ModelTypeTable.id],
                lineageId = row[ModelTypeTable.lineageId],
                modelSnapshotId = row[ModelTypeTable.modelId],
                key = row[ModelTypeTable.key],
                name = row[ModelTypeTable.name],
                description = row[ModelTypeTable.description]
            )
        }
    }
}
