package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.TypeId
import io.medatarun.model.infra.db.tables.RelationshipAttributeTable
import org.jetbrains.exposed.v1.core.ResultRow

data class RelationshipAttributeRecord(
    val snapshotId: AttributeId,
    val lineageId: AttributeId,
    val relationshipSnapshotId: RelationshipId,
    val key: AttributeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val typeSnapshotId: TypeId,
    val optional: Boolean
) {
    companion object {
        fun read(row: ResultRow): RelationshipAttributeRecord {
            return RelationshipAttributeRecord(
                snapshotId = row[RelationshipAttributeTable.id],
                lineageId = row[RelationshipAttributeTable.lineageId],
                relationshipSnapshotId = row[RelationshipAttributeTable.relationshipId],
                key = row[RelationshipAttributeTable.key],
                name = row[RelationshipAttributeTable.name],
                description = row[RelationshipAttributeTable.description],
                typeSnapshotId = row[RelationshipAttributeTable.typeId],
                optional = row[RelationshipAttributeTable.optional]
            )
        }
    }
}
