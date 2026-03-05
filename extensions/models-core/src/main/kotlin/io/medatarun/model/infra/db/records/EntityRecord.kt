package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.ModelId
import io.medatarun.model.infra.db.tables.EntityTable
import org.jetbrains.exposed.v1.core.ResultRow

data class EntityRecord(
    val id: EntityId,
    val modelId: ModelId,
    val key: EntityKey,
    val name: String?,
    val description: String?,
    val identifierAttributeId: AttributeId,
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
