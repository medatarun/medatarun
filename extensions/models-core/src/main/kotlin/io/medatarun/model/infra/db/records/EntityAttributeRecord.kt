package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.TypeId
import io.medatarun.model.infra.db.tables.EntityAttributeTable
import org.jetbrains.exposed.v1.core.ResultRow

data class EntityAttributeRecord(
    val snapshotId: AttributeId,
    val lineageId: AttributeId,
    val entitySnapshotId: EntityId,
    val key: AttributeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val typeSnapshotId: TypeId,
    val optional: Boolean
) {
    companion object {
        fun read(row: ResultRow): EntityAttributeRecord {
            return EntityAttributeRecord(
                snapshotId = row[EntityAttributeTable.id],
                lineageId = row[EntityAttributeTable.lineageId],
                entitySnapshotId = row[EntityAttributeTable.entityId],
                key = row[EntityAttributeTable.key],
                name = row[EntityAttributeTable.name],
                description = row[EntityAttributeTable.description],
                typeSnapshotId = row[EntityAttributeTable.typeId],
                optional = row[EntityAttributeTable.optional]
            )
        }
    }
}
