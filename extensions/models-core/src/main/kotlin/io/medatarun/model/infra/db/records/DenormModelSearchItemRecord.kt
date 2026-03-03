package io.medatarun.model.infra.db.records

import io.medatarun.model.infra.db.tables.DenormModelSearchItemTable
import org.jetbrains.exposed.v1.core.ResultRow

data class DenormModelSearchItemRecord(
    val id: String,
    val itemType: SearchItemType,
    val modelId: String,
    val modelKey: String,
    val modelLabel: String,
    val entityId: String?,
    val entityKey: String?,
    val entityLabel: String?,
    val relationshipId: String?,
    val relationshipKey: String?,
    val relationshipLabel: String?,
    val attributeId: String?,
    val attributeKey: String?,
    val attributeLabel: String?,
    val searchText: String
) {
    companion object {
        fun read(row: ResultRow): DenormModelSearchItemRecord {
            return DenormModelSearchItemRecord(
                id = row[DenormModelSearchItemTable.id],
                itemType = SearchItemType.valueOfCode(row[DenormModelSearchItemTable.itemType]),
                modelId = row[DenormModelSearchItemTable.modelId],
                modelKey = row[DenormModelSearchItemTable.modelKey],
                modelLabel = row[DenormModelSearchItemTable.modelLabel],
                entityId = row[DenormModelSearchItemTable.entityId],
                entityKey = row[DenormModelSearchItemTable.entityKey],
                entityLabel = row[DenormModelSearchItemTable.entityLabel],
                relationshipId = row[DenormModelSearchItemTable.relationshipId],
                relationshipKey = row[DenormModelSearchItemTable.relationshipKey],
                relationshipLabel = row[DenormModelSearchItemTable.relationshipLabel],
                attributeId = row[DenormModelSearchItemTable.attributeId],
                attributeKey = row[DenormModelSearchItemTable.attributeKey],
                attributeLabel = row[DenormModelSearchItemTable.attributeLabel],
                searchText = row[DenormModelSearchItemTable.searchText]
            )
        }
    }
}
