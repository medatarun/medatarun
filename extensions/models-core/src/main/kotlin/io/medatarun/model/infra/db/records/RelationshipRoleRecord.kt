package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipRoleId
import io.medatarun.model.domain.RelationshipRoleKey
import io.medatarun.model.infra.db.tables.RelationshipRoleTable
import org.jetbrains.exposed.v1.core.ResultRow

data class RelationshipRoleRecord(
    val snapshotId: RelationshipRoleId,
    val lineageId: RelationshipRoleId,
    val relationshipSnapshotId: RelationshipId,
    val key: RelationshipRoleKey,
    val entitySnapshotId: EntityId,
    val name: LocalizedText?,
    val cardinality: String
) {
    companion object {
        fun read(row: ResultRow): RelationshipRoleRecord {
            return RelationshipRoleRecord(
                snapshotId = row[RelationshipRoleTable.id],
                lineageId = row[RelationshipRoleTable.lineageId],
                relationshipSnapshotId = row[RelationshipRoleTable.relationshipId],
                key = row[RelationshipRoleTable.key],
                entitySnapshotId = row[RelationshipRoleTable.entityId],
                name = row[RelationshipRoleTable.name],
                cardinality = row[RelationshipRoleTable.cardinality]
            )
        }
    }
}
