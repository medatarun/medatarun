package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.tables.RelationshipAttributeTable
import org.jetbrains.exposed.v1.core.ResultRow

data class RelationshipAttributeRecord(
    val snapshotId: AttributeSnapshotId,
    val lineageId: AttributeId,
    val relationshipSnapshotId: RelationshipSnapshotId,
    val key: AttributeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val typeSnapshotId: TypeSnapshotId,
    val optional: Boolean
) {
    companion object {
        fun read(row: ResultRow): RelationshipAttributeRecord {
            return RelationshipAttributeRecord(
                snapshotId = row[RelationshipAttributeTable.id],
                lineageId = row[RelationshipAttributeTable.lineageId],
                relationshipSnapshotId = row[RelationshipAttributeTable.relationshipSnapshotId],
                key = row[RelationshipAttributeTable.key],
                name = row[RelationshipAttributeTable.name],
                description = row[RelationshipAttributeTable.description],
                typeSnapshotId = row[RelationshipAttributeTable.typeSnapshotId],
                optional = row[RelationshipAttributeTable.optional]
            )
        }
    }
}
