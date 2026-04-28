package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.tables.RelationshipTable
import org.jetbrains.exposed.v1.core.ResultRow

data class RelationshipRecord(
    val snapshotId: RelationshipSnapshotId,
    val lineageId: RelationshipId,
    val modelSnapshotId: ModelSnapshotId,
    val key: RelationshipKey,
    val name: TextSingleLine?,
    val description: TextMarkdown?
) {
    companion object {
        fun read(row: ResultRow): RelationshipRecord {
            return RelationshipRecord(
                snapshotId = row[RelationshipTable.id],
                lineageId = row[RelationshipTable.lineageId],
                modelSnapshotId = row[RelationshipTable.modelSnapshotId],
                key = row[RelationshipTable.key],
                name = row[RelationshipTable.name],
                description = row[RelationshipTable.description]
            )
        }
    }
}
