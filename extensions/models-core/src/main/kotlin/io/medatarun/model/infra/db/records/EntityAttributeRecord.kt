package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.tables.EntityAttributeTable
import org.jetbrains.exposed.v1.core.ResultRow

data class EntityAttributeRecord(
    val snapshotId: AttributeSnapshotId,
    val lineageId: AttributeId,
    val entitySnapshotId: EntitySnapshotId,
    val key: AttributeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val typeSnapshotId: TypeSnapshotId,
    val optional: Boolean
) {
    companion object {
        fun read(row: ResultRow): EntityAttributeRecord {
            return EntityAttributeRecord(
                snapshotId = row[EntityAttributeTable.id],
                lineageId = row[EntityAttributeTable.lineageId],
                entitySnapshotId = row[EntityAttributeTable.entitySnapshotId],
                key = row[EntityAttributeTable.key],
                name = row[EntityAttributeTable.name],
                description = row[EntityAttributeTable.description],
                typeSnapshotId = row[EntityAttributeTable.typeSnapshotId],
                optional = row[EntityAttributeTable.optional]
            )
        }
    }
}
