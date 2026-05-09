package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.tables.BusinessKeyTable
import io.medatarun.type.commons.text.TextMarkdown
import io.medatarun.type.commons.text.TextSingleLine
import org.jetbrains.exposed.v1.core.ResultRow

data class BusinessKeyRecord(
    val snapshotId: BusinessKeySnapshotId,
    val lineageId: BusinessKeyId,
    val modelEntitySnapshotId: EntitySnapshotId,
    val key: BusinessKeyKey,
    val name: TextSingleLine?,
    val description: TextMarkdown?
) {
    companion object {
        fun read(row: ResultRow): BusinessKeyRecord {
            return BusinessKeyRecord(
                snapshotId = row[BusinessKeyTable.id],
                lineageId = row[BusinessKeyTable.lineageId],
                modelEntitySnapshotId = row[BusinessKeyTable.entitySnapshotId],
                key = row[BusinessKeyTable.key],
                name = row[BusinessKeyTable.name],
                description = row[BusinessKeyTable.description]
            )
        }
    }
}
