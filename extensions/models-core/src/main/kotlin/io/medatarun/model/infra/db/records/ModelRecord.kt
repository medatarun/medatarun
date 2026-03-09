package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelAuthority
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.infra.db.tables.ModelTable
import org.jetbrains.exposed.v1.core.ResultRow

data class ModelRecord(
    val id: ModelId,
    val key: ModelKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val version: String,
    val origin: String?,
    val authority: ModelAuthority,
    val documentationHome: String?
) {
    companion object {
        fun read(row: ResultRow): ModelRecord {
            return ModelRecord(
                id = row[ModelTable.id],
                key = row[ModelTable.key],
                name = row[ModelTable.name],
                description = row[ModelTable.description],
                version = row[ModelTable.version],
                origin = row[ModelTable.origin],
                authority = row[ModelTable.authority],
                documentationHome = row[ModelTable.documentationHome]
            )
        }
    }
}
