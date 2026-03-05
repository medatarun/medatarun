package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipKey
import io.medatarun.model.infra.db.tables.RelationshipTable
import org.jetbrains.exposed.v1.core.ResultRow

data class RelationshipRecord(
    val id: RelationshipId,
    val modelId: ModelId,
    val key: RelationshipKey,
    val name: String?,
    val description: String?
) {
    companion object {
        fun read(row: ResultRow): RelationshipRecord {
            return RelationshipRecord(
                id = row[RelationshipTable.id],
                modelId = row[RelationshipTable.modelId],
                key = row[RelationshipTable.key],
                name = row[RelationshipTable.name],
                description = row[RelationshipTable.description]
            )
        }
    }
}
