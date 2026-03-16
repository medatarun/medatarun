package io.medatarun.model.infra.db

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.search.normalizeModelSearchText
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.tables.*
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.key.Key
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

internal class ModelStorageDbSearchWrite(
    private val dbConnectionFactory: DbConnectionFactory,
    private val enabled: Boolean = true
) {
    fun upsertModelSearchItem(modelId: ModelId) {
        if (!enabled) return
        dbConnectionFactory.withExposed {
            upsertModelSearchItemRow(modelId)
        }
    }

    fun deleteModelBranch(modelId: ModelId) {
        if (!enabled) return
        dbConnectionFactory.withExposed {
            deleteRowsByModelId(modelId)
        }
    }

    fun upsertEntitySearchItem(modelId: ModelId, entityId: EntityId) {
        if (!enabled) return
        dbConnectionFactory.withExposed {
            upsertEntitySearchItemRow(modelId, entityId)
        }
    }

    fun deleteEntityBranch(entityId: EntityId) {
        if (!enabled) return
        dbConnectionFactory.withExposed {
            deleteRowsByEntityId(entityId)
        }
    }

    fun upsertEntityAttributeSearchItem(modelId: ModelId, attributeId: AttributeId) {
        if (!enabled) return
        dbConnectionFactory.withExposed {
            upsertEntityAttributeSearchItemRow(modelId, attributeId)
        }
    }

    fun deleteEntityAttributeSearchItem(attributeId: AttributeId) {
        if (!enabled) return
        dbConnectionFactory.withExposed {
            deleteSearchItemById(searchItemIdForEntityAttribute(attributeId))
        }
    }

    fun upsertRelationshipSearchItem(modelId: ModelId, relationshipId: RelationshipId) {
        if (!enabled) return
        dbConnectionFactory.withExposed {
            upsertRelationshipSearchItemRow(modelId, relationshipId)
        }
    }

    fun deleteRelationshipBranch(relationshipId: RelationshipId) {
        if (!enabled) return
        dbConnectionFactory.withExposed {
            deleteRowsByRelationshipId(relationshipId)
        }
    }

    fun upsertRelationshipAttributeSearchItem(modelId: ModelId, attributeId: AttributeId) {
        if (!enabled) return
        dbConnectionFactory.withExposed {
            upsertRelationshipAttributeSearchItemRow(modelId, attributeId)
        }
    }

    fun deleteRelationshipAttributeSearchItem(attributeId: AttributeId) {
        if (!enabled) return
        dbConnectionFactory.withExposed {
            deleteSearchItemById(searchItemIdForRelationshipAttribute(attributeId))
        }
    }

    private fun upsertModelSearchItemRow(modelId: ModelId) {
        val row = ModelSnapshotTable.selectAll()
            .where { (ModelSnapshotTable.modelId eq modelId) and (ModelSnapshotTable.snapshotKind eq "CURRENT_HEAD") }
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
                modelId = modelId,
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
            ModelTagTable.select(ModelTagTable.tagId)
                .where { ModelTagTable.modelSnapshotId eq ModelId.fromString(modelRecord.snapshotId) }
                .map { it[ModelTagTable.tagId] }
        )
    }

    private fun upsertEntitySearchItemRow(modelId: ModelId, entityId: EntityId) {
        val modelSnapshotIdValue = currentHeadModelSnapshotId(modelId)
        val row = EntityTable.selectAll()
            .where { (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotIdValue) }
            .singleOrNull()
        if (row == null) {
            deleteSearchItemById(searchItemIdForEntity(entityId))
            return
        }
        val entityRecord = EntityRecord.read(row)
        val modelRecord = loadModelRecordBySnapshotId(entityRecord.modelSnapshotId)

        replaceSearchItem(
            DenormModelSearchItemRecord(
                id = searchItemIdForEntity(entityId),
                itemType = SearchItemType.ENTITY,
                modelId = modelRecord.modelId,
                modelKey = modelRecord.key,
                modelLabel = modelLabelFromRecord(modelRecord),
                entityId = entityId,
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
            EntityTagTable
                .select(EntityTagTable.tagId)
                .where { EntityTagTable.entitySnapshotId eq entityRecord.snapshotId }
                .map { it[EntityTagTable.tagId] }
        )
    }

    private fun upsertEntityAttributeSearchItemRow(modelId: ModelId, attributeId: AttributeId) {
        val modelSnapshotIdValue = currentHeadModelSnapshotId(modelId)
        val row = EntityAttributeTable.join(
            EntityTable,
            JoinType.INNER,
            EntityAttributeTable.entitySnapshotId,
            EntityTable.id
        ).selectAll()
            .where {
                (EntityAttributeTable.lineageId eq attributeId) and (EntityTable.modelSnapshotId eq modelSnapshotIdValue)
            }
            .singleOrNull()
        if (row == null) {
            deleteSearchItemById(searchItemIdForEntityAttribute(attributeId))
            return
        }
        val attributeRecord = EntityAttributeRecord.read(row)

        val entityRow = EntityTable.selectAll()
            .where { EntityTable.id eq attributeRecord.entitySnapshotId }
            .singleOrNull()
            ?: throw ModelStorageDbSearchMissingSourceRowException("entity", attributeRecord.entitySnapshotId)
        val entityRecord = EntityRecord.read(entityRow)
        val modelRecord = loadModelRecordBySnapshotId(entityRecord.modelSnapshotId)

        replaceSearchItem(
            DenormModelSearchItemRecord(
                id = searchItemIdForEntityAttribute(attributeId),
                itemType = SearchItemType.ENTITY_ATTRIBUTE,
                modelId = modelRecord.modelId,
                modelKey = modelRecord.key,
                modelLabel = modelLabelFromRecord(modelRecord),
                entityId = entityRecord.lineageId,
                entityKey = entityRecord.key,
                entityLabel = entityLabelFromRecord(entityRecord),
                relationshipId = null,
                relationshipKey = null,
                relationshipLabel = null,
                attributeId = attributeId,
                attributeKey = attributeRecord.key,
                attributeLabel = entityAttributeLabelFromRecord(attributeRecord),
                searchText = buildSearchText(attributeRecord.key, attributeRecord.name, attributeRecord.description)
            ),
            EntityAttributeTagTable
                .select(EntityAttributeTagTable.tagId)
                .where { EntityAttributeTagTable.attributeSnapshotId eq attributeRecord.snapshotId }
                .map{ it[EntityAttributeTagTable.tagId] }
        )
    }

    private fun upsertRelationshipSearchItemRow(modelId: ModelId, relationshipId: RelationshipId) {
        val modelSnapshotIdValue = currentHeadModelSnapshotId(modelId)
        val row = RelationshipTable.selectAll()
            .where { (RelationshipTable.lineageId eq relationshipId) and (RelationshipTable.modelSnapshotId eq modelSnapshotIdValue) }
            .singleOrNull()
        if (row == null) {
            deleteSearchItemById(searchItemIdForRelationship(relationshipId))
            return
        }
        val relationshipRecord = RelationshipRecord.read(row)
        val modelRecord = loadModelRecordBySnapshotId(relationshipRecord.modelSnapshotId)

        replaceSearchItem(
            DenormModelSearchItemRecord(
                id = searchItemIdForRelationship(relationshipId),
                itemType = SearchItemType.RELATIONSHIP,
                modelId = modelRecord.modelId,
                modelKey = modelRecord.key,
                modelLabel = modelLabelFromRecord(modelRecord),
                entityId = null,
                entityKey = null,
                entityLabel = null,
                relationshipId = relationshipId,
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
            RelationshipTagTable
                .select(RelationshipTagTable.tagId)
                .where { RelationshipTagTable.relationshipSnapshotId eq relationshipRecord.snapshotId }
                .map { it[RelationshipTagTable.tagId] }
        )
    }

    private fun upsertRelationshipAttributeSearchItemRow(modelId: ModelId, attributeId: AttributeId) {
        val modelSnapshotIdValue = currentHeadModelSnapshotId(modelId)
        val row = RelationshipAttributeTable.join(
            RelationshipTable,
            JoinType.INNER,
            RelationshipAttributeTable.relationshipSnapshotId,
            RelationshipTable.id
        ).selectAll()
            .where {
                (RelationshipAttributeTable.lineageId eq attributeId) and
                        (RelationshipTable.modelSnapshotId eq modelSnapshotIdValue)
            }
            .singleOrNull()
        if (row == null) {
            deleteSearchItemById(searchItemIdForRelationshipAttribute(attributeId))
            return
        }
        val attributeRecord = RelationshipAttributeRecord.read(row)

        val relationshipRow = RelationshipTable.selectAll()
            .where { RelationshipTable.id eq attributeRecord.relationshipSnapshotId }
            .singleOrNull()
            ?: throw ModelStorageDbSearchMissingSourceRowException("relationship", attributeRecord.relationshipSnapshotId)
        val relationshipRecord = RelationshipRecord.read(relationshipRow)
        val modelRecord = loadModelRecordBySnapshotId(relationshipRecord.modelSnapshotId)

        replaceSearchItem(
            DenormModelSearchItemRecord(
                id = searchItemIdForRelationshipAttribute(attributeId),
                itemType = SearchItemType.RELATIONSHIP_ATTRIBUTE,
                modelId = modelRecord.modelId,
                modelKey = modelRecord.key,
                modelLabel = modelLabelFromRecord(modelRecord),
                entityId = null,
                entityKey = null,
                entityLabel = null,
                relationshipId = relationshipRecord.lineageId,
                relationshipKey = relationshipRecord.key,
                relationshipLabel = relationshipLabelFromRecord(relationshipRecord),
                attributeId = attributeId,
                attributeKey = attributeRecord.key,
                attributeLabel = relationshipAttributeLabelFromRecord(attributeRecord),
                searchText = buildSearchText(attributeRecord.key, attributeRecord.name, attributeRecord.description)
            ),
            RelationshipAttributeTagTable
                .select(RelationshipAttributeTagTable.tagId)
                .where { RelationshipAttributeTagTable.attributeSnapshotId eq attributeRecord.snapshotId }
                .map { it[RelationshipAttributeTagTable.tagId] }
        )
    }

    private fun replaceSearchItem(item: DenormModelSearchItemRecord, tagIds: List<TagId>) {
        deleteSearchItemById(item.id)
        val modelSnapshotIdValue = currentHeadModelSnapshotId(item.modelId)
        DenormModelSearchItemTable.insert { row ->
            row[id] = item.id
            row[itemType] = item.itemType.code
            row[modelSnapshotId] = modelSnapshotIdValue
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
            .where { DenormModelSearchItemTable.modelSnapshotId eq modelId }
            .map { it[DenormModelSearchItemTable.id] }
            .forEach { deleteSearchItemById(it) }
    }

    private fun deleteRowsByEntityId(entityId: EntityId) {
        DenormModelSearchItemTable
            .select(DenormModelSearchItemTable.id)
            .where {
                (DenormModelSearchItemTable.entityId eq entityId) or
                        ((DenormModelSearchItemTable.itemType eq SearchItemType.ENTITY_ATTRIBUTE.code) and
                                (DenormModelSearchItemTable.entityId eq entityId))
            }
            .map { it[DenormModelSearchItemTable.id] }
            .forEach { deleteSearchItemById(it) }
    }

    private fun deleteRowsByRelationshipId(relationshipId: RelationshipId) {
        DenormModelSearchItemTable
            .select(DenormModelSearchItemTable.id)
            .where {
                (DenormModelSearchItemTable.relationshipId eq relationshipId) or
                        ((DenormModelSearchItemTable.itemType eq SearchItemType.RELATIONSHIP_ATTRIBUTE.code) and
                                (DenormModelSearchItemTable.relationshipId eq relationshipId))
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

    private fun buildSearchText(key: Key<*>, name: LocalizedText?, description: LocalizedMarkdown?): String {
        return normalizeModelSearchText(listOfNotNull(key, name, description).joinToString(" "))
    }

    private fun modelLabelFromRecord(record: ModelRecord): String {
        return record.name?.name ?: record.key.value
    }

    private fun entityLabelFromRecord(record: EntityRecord): String {
        return record.name?.name ?: record.key.value
    }

    private fun entityAttributeLabelFromRecord(record: EntityAttributeRecord): String {
        return record.name?.name ?: record.key.value
    }

    private fun relationshipLabelFromRecord(record: RelationshipRecord): String {
        return record.name?.name ?: record.key.value
    }

    private fun relationshipAttributeLabelFromRecord(record: RelationshipAttributeRecord): String {
        return record.name?.name ?: record.key.value
    }

    private fun loadModelRecord(modelId: ModelId): ModelRecord {
        val row = ModelSnapshotTable.selectAll()
            .where { (ModelSnapshotTable.modelId eq modelId) and (ModelSnapshotTable.snapshotKind eq "CURRENT_HEAD") }
            .single()
        return ModelRecord.read(row)
    }

    private fun loadModelRecordBySnapshotId(modelSnapshotId: ModelId): ModelRecord {
        val row = ModelSnapshotTable.selectAll()
            .where { ModelSnapshotTable.id eq modelSnapshotId.asString() }
            .single()
        return ModelRecord.read(row)
    }

    private fun currentHeadModelSnapshotId(modelId: ModelId): ModelId {
        return ModelId.fromString(loadModelRecord(modelId).snapshotId)
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
