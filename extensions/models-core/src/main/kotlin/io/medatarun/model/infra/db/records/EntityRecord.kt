package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.tables.EntityTable
import org.jetbrains.exposed.v1.core.ResultRow

data class EntityRecord(
    val snapshotId: EntitySnapshotId,
    val lineageId: EntityId,
    val modelSnapshotId: ModelSnapshotId,
    val key: EntityKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val identifierAttributeSnapshotId: AttributeSnapshotId,
    val origin: EntityOrigin,
    val documentationHome: String?
) {
    companion object {
        fun read(row: ResultRow): EntityRecord {
            return EntityRecord(
                snapshotId = row[EntityTable.id],
                lineageId = row[EntityTable.lineageId],
                modelSnapshotId = row[EntityTable.modelSnapshotId],
                key = row[EntityTable.key],
                name = row[EntityTable.name],
                description = row[EntityTable.description],
                identifierAttributeSnapshotId = row[EntityTable.identifierAttributeSnapshotId],
                origin = row[EntityTable.origin],
                documentationHome = row[EntityTable.documentationHome]
            )
        }
    }
}
