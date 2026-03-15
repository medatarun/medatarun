package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.ModelStorageDbSearchMissingProjectionReferenceException
import io.medatarun.model.infra.db.tables.DenormModelSearchItemTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow

data class DenormModelSearchItemRecord(
    val id: String,
    val itemType: SearchItemType,
    val modelId: ModelId,
    val modelKey: ModelKey,
    val modelLabel: String,
    val entityId: EntityId?,
    val entityKey: EntityKey?,
    val entityLabel: String?,
    val relationshipId: RelationshipId?,
    val relationshipKey: RelationshipKey?,
    val relationshipLabel: String?,
    val attributeId: AttributeId?,
    val attributeKey: AttributeKey?,
    val attributeLabel: String?,
    val searchText: String
) {

    fun toDomainLocation(): DomainLocation {
        return when (itemType) {
            SearchItemType.MODEL -> {
                ModelLocation(
                    id = modelId,
                    key = modelKey,
                    label = modelLabel
                )
            }

            SearchItemType.ENTITY -> {
                EntityLocation(
                    model = modelLocationFromRecord(this),
                    id = requiredValue(entityId, DenormModelSearchItemTable.entityId),
                    key = requiredValue(entityKey, DenormModelSearchItemTable.entityKey),
                    label = requiredValue(entityLabel, DenormModelSearchItemTable.entityLabel)
                )
            }

            SearchItemType.ENTITY_ATTRIBUTE -> {
                EntityAttributeLocation(
                    entity = EntityLocation(
                        model = modelLocationFromRecord(this),
                        id = requiredValue(entityId, DenormModelSearchItemTable.entityId),
                        key = requiredValue(entityKey, DenormModelSearchItemTable.entityKey),
                        label = requiredValue(entityLabel, DenormModelSearchItemTable.entityLabel)
                    ),
                    id = requiredValue(attributeId, DenormModelSearchItemTable.attributeId),
                    key = requiredValue(attributeKey, DenormModelSearchItemTable.attributeKey),
                    label = requiredValue(attributeLabel, DenormModelSearchItemTable.attributeLabel)
                )
            }

            SearchItemType.RELATIONSHIP -> {
                RelationshipLocation(
                    model = modelLocationFromRecord(this),
                    id = requiredValue(relationshipId, DenormModelSearchItemTable.relationshipId),
                    key = requiredValue(relationshipKey, DenormModelSearchItemTable.relationshipKey),
                    label = requiredValue(relationshipLabel, DenormModelSearchItemTable.relationshipLabel)
                )
            }

            SearchItemType.RELATIONSHIP_ATTRIBUTE -> {
                RelationshipAttributeLocation(
                    relationship = RelationshipLocation(
                        model = modelLocationFromRecord(this),
                        id = requiredValue(relationshipId, DenormModelSearchItemTable.relationshipId),
                        key = requiredValue(relationshipKey, DenormModelSearchItemTable.relationshipKey),
                        label = requiredValue(relationshipLabel, DenormModelSearchItemTable.relationshipLabel)
                    ),
                    id = requiredValue(attributeId, DenormModelSearchItemTable.attributeId),
                    key = requiredValue(attributeKey, DenormModelSearchItemTable.attributeKey),
                    label = requiredValue(attributeLabel, DenormModelSearchItemTable.attributeLabel)
                )
            }


        }

    }

    companion object {
        fun read(row: ResultRow): DenormModelSearchItemRecord {
            return DenormModelSearchItemRecord(
                id = row[DenormModelSearchItemTable.id],
                itemType = SearchItemType.valueOfCode(row[DenormModelSearchItemTable.itemType]),
                modelId = row[DenormModelSearchItemTable.modelSnapshotId],
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

        private fun modelLocationFromRecord(item: DenormModelSearchItemRecord): ModelLocation {
            return ModelLocation(
                id = item.modelId,
                key = item.modelKey,
                label = item.modelLabel
            )
        }

        private fun <T> requiredValue(value: T?, columnName: Column<T?>): T {
            return value ?: throw ModelStorageDbSearchMissingProjectionReferenceException(columnName.name)
        }

    }
}
