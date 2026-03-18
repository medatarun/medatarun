package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.tables.ModelSnapshotTable
import org.jetbrains.exposed.v1.core.ResultRow

data class ModelRecord(
    val snapshotId: ModelSnapshotId,
    val modelId: ModelId,
    val key: ModelKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val version: ModelVersion,
    val origin: ModelOrigin,
    val authority: ModelAuthority,
    val documentationHome: String?
) {
    companion object {
        fun read(row: ResultRow): ModelRecord {
            return ModelRecord(
                snapshotId = row[ModelSnapshotTable.id],
                modelId = row[ModelSnapshotTable.modelId],
                key = row[ModelSnapshotTable.key],
                name = row[ModelSnapshotTable.name],
                description = row[ModelSnapshotTable.description],
                version = row[ModelSnapshotTable.version],
                origin = row[ModelSnapshotTable.origin],
                authority = row[ModelSnapshotTable.authority],
                documentationHome = row[ModelSnapshotTable.documentationHome]
            )
        }
    }
}
