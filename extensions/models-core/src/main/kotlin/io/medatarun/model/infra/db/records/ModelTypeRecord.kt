package io.medatarun.model.infra.db.records

import io.medatarun.model.infra.db.tables.ModelTypeTable
import org.jetbrains.exposed.v1.core.ResultRow

data class ModelTypeRecord(
    val id: String,
    val modelId: String,
    val key: String,
    val name: String?,
    val description: String?
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
