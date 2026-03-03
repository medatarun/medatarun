package io.medatarun.model.infra.db

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.search.normalizeModelSearchText
import io.medatarun.model.infra.db.tables.DenormModelSearchItemTable
import io.medatarun.model.infra.db.tables.DenormModelSearchItemTagTable
import io.medatarun.model.infra.db.records.DenormModelSearchItemRecord
import io.medatarun.model.infra.db.records.EntityAttributeRecord
import io.medatarun.model.infra.db.records.EntityRecord
import io.medatarun.model.infra.db.records.ModelRecord
import io.medatarun.model.infra.db.records.RelationshipAttributeRecord
import io.medatarun.model.infra.db.records.RelationshipRecord
import io.medatarun.model.infra.db.records.SearchItemType
import io.medatarun.model.infra.db.tables.EntityAttributeTable
import io.medatarun.model.infra.db.tables.EntityAttributeTagTable
import io.medatarun.model.infra.db.tables.EntityTable
import io.medatarun.model.infra.db.tables.EntityTagTable
import io.medatarun.model.infra.db.tables.ModelTable
import io.medatarun.model.infra.db.tables.ModelTagTable
import io.medatarun.model.infra.db.tables.RelationshipAttributeTable
import io.medatarun.model.infra.db.tables.RelationshipAttributeTagTable
import io.medatarun.model.infra.db.tables.RelationshipTable
import io.medatarun.model.infra.db.tables.RelationshipTagTable
import io.medatarun.platform.db.DbConnectionFactory
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

internal class ModelStorageDbSearchWrite(
    private val dbConnectionFactory: DbConnectionFactory
) {
        fun upsertModelSearchItem(modelId: ModelId) {
            dbConnectionFactory.withExposed {
                upsertModelSearchItemRow(modelId)
            }
        }

        fun deleteModelBranch(modelId: ModelId) {
            dbConnectionFactory.withExposed {
                deleteRowsByModelId(modelId)
            }
        }

        fun upsertEntitySearchItem(entityId: EntityId) {
            dbConnectionFactory.withExposed {
                upsertEntitySearchItemRow(entityId)
            }
        }

        fun deleteEntityBranch(entityId: EntityId) {
            dbConnectionFactory.withExposed {
                deleteRowsByEntityId(entityId)
            }
        }

        fun upsertEntityAttributeSearchItem(attributeId: AttributeId) {
            dbConnectionFactory.withExposed {
                upsertEntityAttributeSearchItemRow(attributeId)
            }
        }

        fun deleteEntityAttributeSearchItem(attributeId: AttributeId) {
            dbConnectionFactory.withExposed {
                deleteSearchItemById(searchItemIdForEntityAttribute(attributeId))
            }
        }

        fun upsertRelationshipSearchItem(relationshipId: RelationshipId) {
            dbConnectionFactory.withExposed {
                upsertRelationshipSearchItemRow(relationshipId)
            }
        }

        fun deleteRelationshipBranch(relationshipId: RelationshipId) {
            dbConnectionFactory.withExposed {
                deleteRowsByRelationshipId(relationshipId)
            }
        }

        fun upsertRelationshipAttributeSearchItem(attributeId: AttributeId) {
            dbConnectionFactory.withExposed {
                upsertRelationshipAttributeSearchItemRow(attributeId)
            }
        }

        fun deleteRelationshipAttributeSearchItem(attributeId: AttributeId) {
            dbConnectionFactory.withExposed {
                deleteSearchItemById(searchItemIdForRelationshipAttribute(attributeId))
            }
        }

        private fun upsertModelSearchItemRow(modelId: ModelId) {
            val row = ModelTable.selectAll()
                .where { ModelTable.id eq modelId.asString() }
                .singleOrNull()
            if (row == null) {
                deleteSearchItemById(searchItemIdForModel(modelId))
                return
            }
            val modelRecord = ModelRecord.read(row)

            replaceSearchItem(
                DenormModelSearchItemRecord(
                    id = searchItemIdForModel(modelId),
                    itemType = SearchItemType.MODEL,
                    modelId = modelId.asString(),
                    modelKey = modelRecord.key,
                    modelLabel = modelLabelFromRecord(modelRecord),
                    entityId = null,
                    entityKey = null,
                    entityLabel = null,
                    relationshipId = null,
                    relationshipKey = null,
                    relationshipLabel = null,
                    attributeId = null,
                    attributeKey = null,
                    attributeLabel = null,
                    searchText = buildSearchText(modelRecord.key, modelRecord.name, modelRecord.description)
                ),
                loadTagIds(ModelTagTable, ModelTagTable.modelId, modelId.asString(), ModelTagTable.tagId)
            )
        }

        private fun upsertEntitySearchItemRow(entityId: EntityId) {
            val row = EntityTable.selectAll()
                .where { EntityTable.id eq entityId.asString() }
                .singleOrNull()
            if (row == null) {
                deleteSearchItemById(searchItemIdForEntity(entityId))
                return
            }
            val entityRecord = EntityRecord.read(row)
            val modelRecord = loadModelRecord(entityRecord.modelId)

            replaceSearchItem(
                DenormModelSearchItemRecord(
                    id = searchItemIdForEntity(entityId),
                    itemType = SearchItemType.ENTITY,
                    modelId = entityRecord.modelId,
                    modelKey = modelRecord.key,
                    modelLabel = modelLabelFromRecord(modelRecord),
                    entityId = entityId.asString(),
                    entityKey = entityRecord.key,
                    entityLabel = entityLabelFromRecord(entityRecord),
                    relationshipId = null,
                    relationshipKey = null,
                    relationshipLabel = null,
                    attributeId = null,
                    attributeKey = null,
                    attributeLabel = null,
                    searchText = buildSearchText(entityRecord.key, entityRecord.name, entityRecord.description)
                ),
                loadTagIds(EntityTagTable, EntityTagTable.entityId, entityId.asString(), EntityTagTable.tagId)
            )
        }

        private fun upsertEntityAttributeSearchItemRow(attributeId: AttributeId) {
            val row = EntityAttributeTable.selectAll()
                .where { EntityAttributeTable.id eq attributeId.asString() }
                .singleOrNull()
            if (row == null) {
                deleteSearchItemById(searchItemIdForEntityAttribute(attributeId))
                return
            }
            val attributeRecord = EntityAttributeRecord.read(row)

            val entityRow = EntityTable.selectAll()
                .where { EntityTable.id eq attributeRecord.entityId }
                .singleOrNull()
                ?: throw ModelStorageDbSearchMissingSourceRowException("entity", attributeRecord.entityId)
            val entityRecord = EntityRecord.read(entityRow)
            val modelRecord = loadModelRecord(entityRecord.modelId)

            replaceSearchItem(
                DenormModelSearchItemRecord(
                    id = searchItemIdForEntityAttribute(attributeId),
                    itemType = SearchItemType.ENTITY_ATTRIBUTE,
                    modelId = entityRecord.modelId,
                    modelKey = modelRecord.key,
                    modelLabel = modelLabelFromRecord(modelRecord),
                    entityId = attributeRecord.entityId,
                    entityKey = entityRecord.key,
                    entityLabel = entityLabelFromRecord(entityRecord),
                    relationshipId = null,
                    relationshipKey = null,
                    relationshipLabel = null,
                    attributeId = attributeId.asString(),
                    attributeKey = attributeRecord.key,
                    attributeLabel = entityAttributeLabelFromRecord(attributeRecord),
                    searchText = buildSearchText(attributeRecord.key, attributeRecord.name, attributeRecord.description)
                ),
                loadTagIds(
                    EntityAttributeTagTable,
                    EntityAttributeTagTable.attributeId,
                    attributeId.asString(),
                    EntityAttributeTagTable.tagId
                )
            )
        }

        private fun upsertRelationshipSearchItemRow(relationshipId: RelationshipId) {
            val row = RelationshipTable.selectAll()
                .where { RelationshipTable.id eq relationshipId.asString() }
                .singleOrNull()
            if (row == null) {
                deleteSearchItemById(searchItemIdForRelationship(relationshipId))
                return
            }
            val relationshipRecord = RelationshipRecord.read(row)
            val modelRecord = loadModelRecord(relationshipRecord.modelId)

            replaceSearchItem(
                DenormModelSearchItemRecord(
                    id = searchItemIdForRelationship(relationshipId),
                    itemType = SearchItemType.RELATIONSHIP,
                    modelId = relationshipRecord.modelId,
                    modelKey = modelRecord.key,
                    modelLabel = modelLabelFromRecord(modelRecord),
                    entityId = null,
                    entityKey = null,
                    entityLabel = null,
                    relationshipId = relationshipId.asString(),
                    relationshipKey = relationshipRecord.key,
                    relationshipLabel = relationshipLabelFromRecord(relationshipRecord),
                    attributeId = null,
                    attributeKey = null,
                    attributeLabel = null,
                    searchText = buildSearchText(
                        relationshipRecord.key,
                        relationshipRecord.name,
                        relationshipRecord.description
                    )
                ),
                loadTagIds(
                    RelationshipTagTable,
                    RelationshipTagTable.relationshipId,
                    relationshipId.asString(),
                    RelationshipTagTable.tagId
                )
            )
        }

        private fun upsertRelationshipAttributeSearchItemRow(attributeId: AttributeId) {
            val row = RelationshipAttributeTable.selectAll()
                .where { RelationshipAttributeTable.id eq attributeId.asString() }
                .singleOrNull()
            if (row == null) {
                deleteSearchItemById(searchItemIdForRelationshipAttribute(attributeId))
                return
            }
            val attributeRecord = RelationshipAttributeRecord.read(row)

            val relationshipRow = RelationshipTable.selectAll()
                .where { RelationshipTable.id eq attributeRecord.relationshipId }
                .singleOrNull()
                ?: throw ModelStorageDbSearchMissingSourceRowException("relationship", attributeRecord.relationshipId)
            val relationshipRecord = RelationshipRecord.read(relationshipRow)
            val modelRecord = loadModelRecord(relationshipRecord.modelId)

            replaceSearchItem(
                DenormModelSearchItemRecord(
                    id = searchItemIdForRelationshipAttribute(attributeId),
                    itemType = SearchItemType.RELATIONSHIP_ATTRIBUTE,
                    modelId = relationshipRecord.modelId,
                    modelKey = modelRecord.key,
                    modelLabel = modelLabelFromRecord(modelRecord),
                    entityId = null,
                    entityKey = null,
                    entityLabel = null,
                    relationshipId = attributeRecord.relationshipId,
                    relationshipKey = relationshipRecord.key,
                    relationshipLabel = relationshipLabelFromRecord(relationshipRecord),
                    attributeId = attributeId.asString(),
                    attributeKey = attributeRecord.key,
                    attributeLabel = relationshipAttributeLabelFromRecord(attributeRecord),
                    searchText = buildSearchText(attributeRecord.key, attributeRecord.name, attributeRecord.description)
                ),
                loadTagIds(
                    RelationshipAttributeTagTable,
                    RelationshipAttributeTagTable.attributeId,
                    attributeId.asString(),
                    RelationshipAttributeTagTable.tagId
                )
            )
        }

        private fun replaceSearchItem(item: DenormModelSearchItemRecord, tagIds: List<String>) {
            deleteSearchItemById(item.id)
            DenormModelSearchItemTable.insert { row ->
                row[id] = item.id
                row[itemType] = item.itemType.code
                row[modelId] = item.modelId
                row[modelKey] = item.modelKey
                row[modelLabel] = item.modelLabel
                row[entityId] = item.entityId
                row[entityKey] = item.entityKey
                row[entityLabel] = item.entityLabel
                row[relationshipId] = item.relationshipId
                row[relationshipKey] = item.relationshipKey
                row[relationshipLabel] = item.relationshipLabel
                row[attributeId] = item.attributeId
                row[attributeKey] = item.attributeKey
                row[attributeLabel] = item.attributeLabel
                row[searchText] = item.searchText
            }

            tagIds.forEach { tagId ->
                DenormModelSearchItemTagTable.insert { row ->
                    row[searchItemId] = item.id
                    row[DenormModelSearchItemTagTable.tagId] = tagId
                }
            }
        }

        private fun deleteRowsByModelId(modelId: ModelId) {
            DenormModelSearchItemTable
                .select(DenormModelSearchItemTable.id)
                .where { DenormModelSearchItemTable.modelId eq modelId.asString() }
                .map { it[DenormModelSearchItemTable.id] }
                .forEach { deleteSearchItemById(it) }
        }

        private fun deleteRowsByEntityId(entityId: EntityId) {
            DenormModelSearchItemTable
                .select(DenormModelSearchItemTable.id)
                .where {
                    (DenormModelSearchItemTable.entityId eq entityId.asString()) or
                            ((DenormModelSearchItemTable.itemType eq SearchItemType.ENTITY_ATTRIBUTE.code) and
                                    (DenormModelSearchItemTable.entityId eq entityId.asString()))
                }
                .map { it[DenormModelSearchItemTable.id] }
                .forEach { deleteSearchItemById(it) }
        }

        private fun deleteRowsByRelationshipId(relationshipId: RelationshipId) {
            DenormModelSearchItemTable
                .select(DenormModelSearchItemTable.id)
                .where {
                    (DenormModelSearchItemTable.relationshipId eq relationshipId.asString()) or
                            ((DenormModelSearchItemTable.itemType eq SearchItemType.RELATIONSHIP_ATTRIBUTE.code) and
                                    (DenormModelSearchItemTable.relationshipId eq relationshipId.asString()))
                }
                .map { it[DenormModelSearchItemTable.id] }
                .forEach { deleteSearchItemById(it) }
        }

        private fun deleteSearchItemById(searchItemId: String) {
            DenormModelSearchItemTagTable.deleteWhere {
                DenormModelSearchItemTagTable.searchItemId eq searchItemId
            }
            DenormModelSearchItemTable.deleteWhere {
                DenormModelSearchItemTable.id eq searchItemId
            }
        }

        private fun buildSearchText(key: String, name: String?, description: String?): String {
            return normalizeModelSearchText(listOfNotNull(key, name, description).joinToString(" "))
        }

        private fun modelLabelFromRecord(record: ModelRecord): String {
            return record.name ?: record.key
        }

        private fun entityLabelFromRecord(record: EntityRecord): String {
            return record.name ?: record.key
        }

        private fun entityAttributeLabelFromRecord(record: EntityAttributeRecord): String {
            return record.name ?: record.key
        }

        private fun relationshipLabelFromRecord(record: RelationshipRecord): String {
            return record.name ?: record.key
        }

        private fun relationshipAttributeLabelFromRecord(record: RelationshipAttributeRecord): String {
            return record.name ?: record.key
        }

        private fun loadModelRecord(modelId: String): ModelRecord {
            val row = ModelTable.selectAll()
                .where { ModelTable.id eq modelId }
                .single()
            return ModelRecord.read(row)
        }

        private fun <T : Table> loadTagIds(
            table: T,
            ownerColumn: Column<String>,
            ownerId: String,
            tagColumn: Column<String>
        ): List<String> {
            return table.select(tagColumn)
                .where { ownerColumn eq ownerId }
                .map { it[tagColumn] }
        }

        private fun searchItemIdForModel(modelId: ModelId): String {
            return "model:" + modelId.asString()
        }

        private fun searchItemIdForEntity(entityId: EntityId): String {
            return "entity:" + entityId.asString()
        }

        private fun searchItemIdForEntityAttribute(attributeId: AttributeId): String {
            return "entity_attribute:" + attributeId.asString()
        }

        private fun searchItemIdForRelationship(relationshipId: RelationshipId): String {
            return "relationship:" + relationshipId.asString()
        }

        private fun searchItemIdForRelationshipAttribute(attributeId: AttributeId): String {
            return "relationship_attribute:" + attributeId.asString()
        }
}
