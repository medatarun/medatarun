package io.medatarun.model.infra.db.records

import io.medatarun.model.infra.db.tables.EntityTable
import org.jetbrains.exposed.v1.core.ResultRow

data class EntityRecord(
    val id: String,
    val modelId: String,
    val key: String,
    val name: String?,
    val description: String?,
    val identifierAttributeId: String,
    val origin: String?,
    val documentationHome: String?
) {
    companion object {
        fun read(row: ResultRow): EntityRecord {
            return EntityRecord(
                id = row[EntityTable.id],
                modelId = row[EntityTable.modelId],
                key = row[EntityTable.key],
                name = row[EntityTable.name],
                description = row[EntityTable.description],
                identifierAttributeId = row[EntityTable.identifierAttributeId],
                origin = row[EntityTable.origin],
                documentationHome = row[EntityTable.documentationHome]
            )
        }
    }
}
