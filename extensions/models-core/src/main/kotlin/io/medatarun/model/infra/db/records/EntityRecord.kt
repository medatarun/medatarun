package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityOrigin
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelId
import io.medatarun.model.infra.db.tables.EntityTable
import org.jetbrains.exposed.v1.core.ResultRow

data class EntityRecord(
    val id: EntityId,
    val modelId: ModelId,
    val key: EntityKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val identifierAttributeId: AttributeId,
    val origin: EntityOrigin,
    val documentationHome: String?
) {
    companion object {
        fun read(row: ResultRow): EntityRecord {
            return EntityRecord(
                id = row[EntityTable.lineageId],
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
