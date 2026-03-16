package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.domain.search.normalizeModelSearchText
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.records.SearchItemType
import io.medatarun.model.infra.db.snapshots.SnapshotSelector
import io.medatarun.model.infra.db.tables.*
import io.medatarun.model.infra.db.tables.DenormModelSearchItemTable
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.key.Key
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

internal class ModelStorageDbSearchWrite(
    private val enabled: Boolean = true
) {
    fun upsertModelSearchItem(modelSnapshotId: ModelSnapshotId) {
        if (!enabled) return

        // Find the model record to update
        val modelRecord = ModelSnapshotTable.selectAll()
            .where { SnapshotSelector.ById(modelSnapshotId).criterion() }
            .singleOrNull()
            ?.let { ModelRecord.read(it) }

        // If found update it, but ignore issues if not found
        if (modelRecord != null) {
            replaceSearchItem(
                DenormModelSearchItemRecord(
                    id = searchItemIdForModel(modelRecord.modelId),
                    itemType = SearchItemType.MODEL,
                    modelSnapshotId = modelRecord.snapshotId,
                    modelId = modelRecord.modelId,
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
                    .where { ModelTagTable.modelSnapshotId eq modelRecord.snapshotId }
                    .map { it[ModelTagTable.tagId] }
            )
        }
    }


    /**
     * Deletes the model and every row that matches branch of this model (entity, attributes, etc.)
     */
    fun deleteModelBranch(modelId: ModelId) {
        if (!enabled) return
        val row = ModelSnapshotTable.select(ModelSnapshotTable.id)
            .where { SnapshotSelector.CurrentHeadByModelId(modelId).criterion() }
            .singleOrNull()
        if (row != null) {
            val modelSnapshotIdValue = row[ModelSnapshotTable.id]
            DenormModelSearchItemTable
                .select(DenormModelSearchItemTable.id)
                .where { DenormModelSearchItemTable.modelSnapshotId eq modelSnapshotIdValue }
                .map { it[DenormModelSearchItemTable.id] }
                .forEach { deleteSearchItemById(it) }
        }
    }

    fun upsertEntitySearchItem(modelSnapshotId: ModelSnapshotId, entityId: EntityId) {
        if (!enabled) return
        val row = EntityTable.join(
            ModelSnapshotTable,
            JoinType.INNER,
            EntityTable.modelSnapshotId,
            ModelSnapshotTable.id
        ).selectAll()
            .where { (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId) }
            .singleOrNull()
        if (row == null) {
            deleteSearchItemById(searchItemIdForEntity(entityId))
            return
        }
        val entityRecord = EntityRecord.read(row)
        val modelRecord = ModelRecord.read(row)

        replaceSearchItem(
            DenormModelSearchItemRecord(
                id = searchItemIdForEntity(entityId),
                itemType = SearchItemType.ENTITY,
                modelSnapshotId = modelRecord.snapshotId,
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

    fun deleteEntityBranch(entityId: EntityId) {
        if (!enabled) return
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

    fun upsertEntityAttributeSearchItem(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId
    ) {
        if (!enabled) return
        val row = EntityAttributeTable.join(
            EntityTable,
            JoinType.INNER,
            EntityAttributeTable.entitySnapshotId,
            EntityTable.id
        ).join(
            ModelSnapshotTable,
            JoinType.INNER,
            EntityTable.modelSnapshotId,
            ModelSnapshotTable.id
        ).selectAll()
            .where {
                (EntityAttributeTable.lineageId eq attributeId) and
                        (EntityTable.lineageId eq entityId) and
                        (EntityTable.modelSnapshotId eq modelSnapshotId)
            }
            .singleOrNull()
        if (row == null) {
            deleteSearchItemById(searchItemIdForEntityAttribute(attributeId))
            return
        }
        val attributeRecord = EntityAttributeRecord.read(row)
        val entityRecord = EntityRecord.read(row)
        val modelRecord = ModelRecord.read(row)

        replaceSearchItem(
            DenormModelSearchItemRecord(
                id = searchItemIdForEntityAttribute(attributeId),
                itemType = SearchItemType.ENTITY_ATTRIBUTE,
                modelSnapshotId = modelRecord.snapshotId,
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

    fun deleteEntityAttributeSearchItem(attributeId: AttributeId) {
        if (!enabled) return
        deleteSearchItemById(searchItemIdForEntityAttribute(attributeId))
    }

    fun upsertRelationshipSearchItem(modelSnapshotId: ModelSnapshotId, relationshipId: RelationshipId) {
        if (!enabled) return
        val row = RelationshipTable.join(
            ModelSnapshotTable,
            JoinType.INNER,
            RelationshipTable.modelSnapshotId,
            ModelSnapshotTable.id
        ).selectAll()
            .where { (RelationshipTable.lineageId eq relationshipId) and (RelationshipTable.modelSnapshotId eq modelSnapshotId) }
            .singleOrNull()
        if (row == null) {
            deleteSearchItemById(searchItemIdForRelationship(relationshipId))
            return
        }
        val relationshipRecord = RelationshipRecord.read(row)
        val modelRecord = ModelRecord.read(row)

        replaceSearchItem(
            DenormModelSearchItemRecord(
                id = searchItemIdForRelationship(relationshipId),
                itemType = SearchItemType.RELATIONSHIP,
                modelSnapshotId = modelRecord.snapshotId,
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

    fun deleteRelationshipBranch(relationshipId: RelationshipId) {
        if (!enabled) return
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

    fun upsertRelationshipAttributeSearchItem(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId
    ) {
        if (!enabled) return
        val row = RelationshipAttributeTable.join(
            RelationshipTable,
            JoinType.INNER,
            RelationshipAttributeTable.relationshipSnapshotId,
            RelationshipTable.id
        ).join(
            ModelSnapshotTable,
            JoinType.INNER,
            RelationshipTable.modelSnapshotId,
            ModelSnapshotTable.id
        ).selectAll()
            .where {
                (RelationshipAttributeTable.lineageId eq attributeId) and
                        (RelationshipTable.lineageId eq relationshipId) and
                        (RelationshipTable.modelSnapshotId eq modelSnapshotId)
            }
            .singleOrNull()
        if (row == null) {
            deleteSearchItemById(searchItemIdForRelationshipAttribute(attributeId))
            return
        }
        val attributeRecord = RelationshipAttributeRecord.read(row)
        val relationshipRecord = RelationshipRecord.read(row)
        val modelRecord = ModelRecord.read(row)

        replaceSearchItem(
            DenormModelSearchItemRecord(
                id = searchItemIdForRelationshipAttribute(attributeId),
                itemType = SearchItemType.RELATIONSHIP_ATTRIBUTE,
                modelSnapshotId = modelRecord.snapshotId,
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

    fun deleteRelationshipAttributeSearchItem(attributeId: AttributeId) {
        if (!enabled) return
        deleteSearchItemById(searchItemIdForRelationshipAttribute(attributeId))
    }




    private fun replaceSearchItem(item: DenormModelSearchItemRecord, tagIds: List<TagId>) {
        deleteSearchItemById(item.id)
        DenormModelSearchItemTable.insert { row ->
            row[id] = item.id
            row[itemType] = item.itemType.code
            row[modelSnapshotId] = item.modelSnapshotId
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
