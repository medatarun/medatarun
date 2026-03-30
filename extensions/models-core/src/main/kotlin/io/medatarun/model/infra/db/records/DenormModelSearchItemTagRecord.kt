package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.ModelSearchItemSnapshotId
import io.medatarun.model.infra.db.tables.DenormModelSearchItemTagTable
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.ResultRow

data class DenormModelSearchItemTagRecord(
    val searchItemId: ModelSearchItemSnapshotId,
    val tagId: TagId
) {
    companion object {
        fun read(row: ResultRow): DenormModelSearchItemTagRecord {
            return DenormModelSearchItemTagRecord(
                searchItemId = row[DenormModelSearchItemTagTable.searchItemId],
                tagId = row[DenormModelSearchItemTagTable.tagId]
            )
        }
    }
}
