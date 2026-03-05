package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.TypeId
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.infra.db.tables.ModelTypeTable
import org.jetbrains.exposed.v1.core.ResultRow

data class ModelTypeRecord(
    val id: TypeId,
    val modelId: ModelId,
    val key: TypeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
) {
    companion object {
        fun read(row: ResultRow): ModelTypeRecord {
            return ModelTypeRecord(
                id = row[ModelTypeTable.id],
                modelId = row[ModelTypeTable.modelId],
                key = row[ModelTypeTable.key],
                name = row[ModelTypeTable.name],
                description = row[ModelTypeTable.description]
            )
        }
    }
}
