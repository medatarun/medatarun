package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.tables.RelationshipRoleTable
import org.jetbrains.exposed.v1.core.ResultRow

data class RelationshipRoleRecord(
    val snapshotId: RelationshipRoleSnapshotId,
    val lineageId: RelationshipRoleId,
    val relationshipSnapshotId: RelationshipSnapshotId,
    val key: RelationshipRoleKey,
    val entitySnapshotId: EntitySnapshotId,
    val name: TextSingleLine?,
    val cardinality: String
) {
    companion object {
        fun read(row: ResultRow): RelationshipRoleRecord {
            return RelationshipRoleRecord(
                snapshotId = row[RelationshipRoleTable.id],
                lineageId = row[RelationshipRoleTable.lineageId],
                relationshipSnapshotId = row[RelationshipRoleTable.relationshipSnapshotId],
                key = row[RelationshipRoleTable.key],
                entitySnapshotId = row[RelationshipRoleTable.entitySnapshotId],
                name = row[RelationshipRoleTable.name],
                cardinality = row[RelationshipRoleTable.cardinality]
            )
        }
    }
}
