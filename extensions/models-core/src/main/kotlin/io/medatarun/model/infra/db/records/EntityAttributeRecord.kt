package io.medatarun.model.infra.db.records

import io.medatarun.model.infra.db.tables.EntityAttributeTable
import org.jetbrains.exposed.v1.core.ResultRow

data class EntityAttributeRecord(
    val id: String,
    val entityId: String,
    val key: String,
    val name: String?,
    val description: String?,
    val typeId: String,
    val optional: Boolean
) {
    companion object {
        fun read(row: ResultRow): EntityAttributeRecord {
            return EntityAttributeRecord(
                id = row[EntityAttributeTable.id],
                entityId = row[EntityAttributeTable.entityId],
                key = row[EntityAttributeTable.key],
                name = row[EntityAttributeTable.name],
                description = row[EntityAttributeTable.description],
                typeId = row[EntityAttributeTable.typeId],
                optional = row[EntityAttributeTable.optional]
            )
        }
    }
}
