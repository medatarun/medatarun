package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipRoleId
import io.medatarun.model.infra.db.tables.RelationshipRoleTable
import org.jetbrains.exposed.v1.core.ResultRow

data class RelationshipRoleRecord(
    val id: RelationshipRoleId,
    val relationshipId: RelationshipId,
    val key: String,
    val entityId: EntityId,
    val name: String?,
    val cardinality: String
) {
    companion object {
        fun read(row: ResultRow): RelationshipRoleRecord {
            return RelationshipRoleRecord(
                id = row[RelationshipRoleTable.id],
                relationshipId = row[RelationshipRoleTable.relationshipId],
                key = row[RelationshipRoleTable.key],
                entityId = row[RelationshipRoleTable.entityId],
                name = row[RelationshipRoleTable.name],
                cardinality = row[RelationshipRoleTable.cardinality]
            )
        }
    }
}
