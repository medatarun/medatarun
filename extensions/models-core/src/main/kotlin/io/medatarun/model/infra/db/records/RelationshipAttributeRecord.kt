package io.medatarun.model.infra.db.records

import io.medatarun.model.infra.db.tables.RelationshipAttributeTable
import org.jetbrains.exposed.v1.core.ResultRow

data class RelationshipAttributeRecord(
    val id: String,
    val relationshipId: String,
    val key: String,
    val name: String?,
    val description: String?,
    val typeId: String,
    val optional: Boolean
) {
    companion object {
        fun read(row: ResultRow): RelationshipAttributeRecord {
            return RelationshipAttributeRecord(
                id = row[RelationshipAttributeTable.id],
                relationshipId = row[RelationshipAttributeTable.relationshipId],
                key = row[RelationshipAttributeTable.key],
                name = row[RelationshipAttributeTable.name],
                description = row[RelationshipAttributeTable.description],
                typeId = row[RelationshipAttributeTable.typeId],
                optional = row[RelationshipAttributeTable.optional]
            )
        }
    }
}
