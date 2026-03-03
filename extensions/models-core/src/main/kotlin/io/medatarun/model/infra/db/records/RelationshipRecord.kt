package io.medatarun.model.infra.db.records

import io.medatarun.model.infra.db.tables.RelationshipTable
import org.jetbrains.exposed.v1.core.ResultRow

data class RelationshipRecord(
    val id: String,
    val modelId: String,
    val key: String,
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
