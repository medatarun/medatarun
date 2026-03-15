package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelAuthority
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelOrigin
import io.medatarun.model.infra.db.tables.ModelSnapshotTable
import org.jetbrains.exposed.v1.core.ResultRow

data class ModelRecord(
    val id: ModelId,
    val key: ModelKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val version: String?,
    val origin: ModelOrigin,
    val authority: ModelAuthority,
    val documentationHome: String?
) {
    companion object {
        fun read(row: ResultRow): ModelRecord {
            return ModelRecord(
                id = row[ModelSnapshotTable.modelId],
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
