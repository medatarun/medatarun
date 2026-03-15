package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipKey
import io.medatarun.model.infra.db.tables.RelationshipTable
import org.jetbrains.exposed.v1.core.ResultRow

data class RelationshipRecord(
    val id: RelationshipId,
    val modelId: ModelId,
    val key: RelationshipKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
) {
    companion object {
        fun read(row: ResultRow): RelationshipRecord {
            return RelationshipRecord(
                id = row[RelationshipTable.lineageId],
                modelId = row[RelationshipTable.modelId],
                key = row[RelationshipTable.key],
                name = row[RelationshipTable.name],
                description = row[RelationshipTable.description]
            )
        }
    }
}
