package io.medatarun.model.infra.db.records

import io.medatarun.model.infra.db.tables.DenormModelSearchItemTagTable
import org.jetbrains.exposed.v1.core.ResultRow

data class DenormModelSearchItemTagRecord(
    val searchItemId: String,
    val tagId: String
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
