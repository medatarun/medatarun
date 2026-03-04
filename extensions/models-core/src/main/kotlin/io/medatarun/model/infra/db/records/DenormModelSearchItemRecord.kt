package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.DomainLocation
import io.medatarun.model.domain.EntityAttributeLocation
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityLocation
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelLocation
import io.medatarun.model.domain.RelationshipAttributeLocation
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipKey
import io.medatarun.model.domain.RelationshipLocation
import io.medatarun.model.infra.db.ModelStorageDbSearchMissingProjectionReferenceException
import io.medatarun.model.infra.db.tables.DenormModelSearchItemTable
import org.jetbrains.exposed.v1.core.Column
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

    fun toDomainLocation(): DomainLocation {
        return when (itemType) {
            SearchItemType.MODEL -> {
                ModelLocation(
                    id = ModelId.fromString(modelId),
                    key = ModelKey(modelKey),
                    label = modelLabel
                )
            }

            SearchItemType.ENTITY -> {
                EntityLocation(
                    model = modelLocationFromRecord(this),
                    id = EntityId.fromString(requiredValue(entityId, DenormModelSearchItemTable.entityId)),
                    key = EntityKey(requiredValue(entityKey, DenormModelSearchItemTable.entityKey)),
                    label = requiredValue(entityLabel, DenormModelSearchItemTable.entityLabel)
                )
            }

            SearchItemType.ENTITY_ATTRIBUTE -> {
                EntityAttributeLocation(
                    entity = EntityLocation(
                        model = modelLocationFromRecord(this),
                        id = EntityId.fromString(requiredValue(entityId, DenormModelSearchItemTable.entityId)),
                        key = EntityKey(requiredValue(entityKey, DenormModelSearchItemTable.entityKey)),
                        label = requiredValue(entityLabel, DenormModelSearchItemTable.entityLabel)
                    ),
                    id = AttributeId.fromString(requiredValue(attributeId, DenormModelSearchItemTable.attributeId)),
                    key = AttributeKey(requiredValue(attributeKey, DenormModelSearchItemTable.attributeKey)),
                    label = requiredValue(attributeLabel, DenormModelSearchItemTable.attributeLabel)
                )
            }

            SearchItemType.RELATIONSHIP -> {
                RelationshipLocation(
                    model = modelLocationFromRecord(this),
                    id = RelationshipId.fromString(requiredValue(relationshipId, DenormModelSearchItemTable.relationshipId)),
                    key = RelationshipKey(requiredValue(relationshipKey, DenormModelSearchItemTable.relationshipKey)),
                    label = requiredValue(relationshipLabel, DenormModelSearchItemTable.relationshipLabel)
                )
            }

            SearchItemType.RELATIONSHIP_ATTRIBUTE -> {
                RelationshipAttributeLocation(
                    relationship = RelationshipLocation(
                        model = modelLocationFromRecord(this),
                        id = RelationshipId.fromString(requiredValue(relationshipId, DenormModelSearchItemTable.relationshipId)),
                        key = RelationshipKey(requiredValue(relationshipKey, DenormModelSearchItemTable.relationshipKey)),
                        label = requiredValue(relationshipLabel, DenormModelSearchItemTable.relationshipLabel)
                    ),
                    id = AttributeId.fromString(requiredValue(attributeId, DenormModelSearchItemTable.attributeId)),
                    key = AttributeKey(requiredValue(attributeKey, DenormModelSearchItemTable.attributeKey)),
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
        private fun modelLocationFromRecord(item: DenormModelSearchItemRecord): ModelLocation {
            return ModelLocation(
                id = ModelId.fromString(item.modelId),
                key = ModelKey(item.modelKey),
                label = item.modelLabel
            )
        }

        private fun requiredValue(value: String?, columnName: Column<String?>): String {
            return value ?: throw ModelStorageDbSearchMissingProjectionReferenceException(columnName.name)
        }

    }
}
