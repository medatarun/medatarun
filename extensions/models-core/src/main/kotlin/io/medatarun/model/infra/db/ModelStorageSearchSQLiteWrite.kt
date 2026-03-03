package io.medatarun.model.infra.db

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.search.normalizeModelSearchText
import io.medatarun.platform.db.DbConnectionFactory
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

internal class ModelStorageSearchSQLiteWrite(
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
            val row = ModelStorageSQLite.Companion.ModelTable.selectAll()
                .where { ModelStorageSQLite.Companion.ModelTable.id eq modelId.asString() }
                .singleOrNull()
            if (row == null) {
                deleteSearchItemById(searchItemIdForModel(modelId))
                return
            }

            replaceSearchItem(
                DenormModelSearchItem(
                    id = searchItemIdForModel(modelId),
                    itemType = SearchItemType.MODEL.code,
                    modelId = modelId.asString(),
                    modelKey = row[ModelStorageSQLite.Companion.ModelTable.key],
                    modelLabel = modelLabelFromRow(row),
                    entityId = null,
                    entityKey = null,
                    entityLabel = null,
                    relationshipId = null,
                    relationshipKey = null,
                    relationshipLabel = null,
                    attributeId = null,
                    attributeKey = null,
                    attributeLabel = null,
                    searchText = buildSearchText(row[ModelStorageSQLite.Companion.ModelTable.key], row[ModelStorageSQLite.Companion.ModelTable.name], row[ModelStorageSQLite.Companion.ModelTable.description])
                ),
                loadTagIds(ModelStorageSQLite.Companion.ModelTagTable, ModelStorageSQLite.Companion.ModelTagTable.modelId, modelId.asString(), ModelStorageSQLite.Companion.ModelTagTable.tagId)
            )
        }

        private fun upsertEntitySearchItemRow(entityId: EntityId) {
            val row = ModelStorageSQLite.Companion.EntityTable.selectAll()
                .where { ModelStorageSQLite.Companion.EntityTable.id eq entityId.asString() }
                .singleOrNull()
            if (row == null) {
                deleteSearchItemById(searchItemIdForEntity(entityId))
                return
            }

            replaceSearchItem(
                DenormModelSearchItem(
                    id = searchItemIdForEntity(entityId),
                    itemType = SearchItemType.ENTITY.code,
                    modelId = row[ModelStorageSQLite.Companion.EntityTable.modelId],
                    modelKey = modelKeyById(row[ModelStorageSQLite.Companion.EntityTable.modelId]),
                    modelLabel = modelLabelById(row[ModelStorageSQLite.Companion.EntityTable.modelId]),
                    entityId = entityId.asString(),
                    entityKey = row[ModelStorageSQLite.Companion.EntityTable.key],
                    entityLabel = entityLabelFromRow(row),
                    relationshipId = null,
                    relationshipKey = null,
                    relationshipLabel = null,
                    attributeId = null,
                    attributeKey = null,
                    attributeLabel = null,
                    searchText = buildSearchText(row[ModelStorageSQLite.Companion.EntityTable.key], row[ModelStorageSQLite.Companion.EntityTable.name], row[ModelStorageSQLite.Companion.EntityTable.description])
                ),
                loadTagIds(ModelStorageSQLite.Companion.EntityTagTable, ModelStorageSQLite.Companion.EntityTagTable.entityId, entityId.asString(), ModelStorageSQLite.Companion.EntityTagTable.tagId)
            )
        }

        private fun upsertEntityAttributeSearchItemRow(attributeId: AttributeId) {
            val row = ModelStorageSQLite.Companion.EntityAttributeTable.selectAll()
                .where { ModelStorageSQLite.Companion.EntityAttributeTable.id eq attributeId.asString() }
                .singleOrNull()
            if (row == null) {
                deleteSearchItemById(searchItemIdForEntityAttribute(attributeId))
                return
            }

            val entityRow = ModelStorageSQLite.Companion.EntityTable.select(ModelStorageSQLite.Companion.EntityTable.modelId)
                .where { ModelStorageSQLite.Companion.EntityTable.id eq row[ModelStorageSQLite.Companion.EntityAttributeTable.entityId] }
                .singleOrNull()
                ?: throw ModelStorageSearchSQLiteMissingSourceRowException("entity", row[ModelStorageSQLite.Companion.EntityAttributeTable.entityId])

            replaceSearchItem(
                DenormModelSearchItem(
                    id = searchItemIdForEntityAttribute(attributeId),
                    itemType = SearchItemType.ENTITY_ATTRIBUTE.code,
                    modelId = entityRow[ModelStorageSQLite.Companion.EntityTable.modelId],
                    modelKey = modelKeyById(entityRow[ModelStorageSQLite.Companion.EntityTable.modelId]),
                    modelLabel = modelLabelById(entityRow[ModelStorageSQLite.Companion.EntityTable.modelId]),
                    entityId = row[ModelStorageSQLite.Companion.EntityAttributeTable.entityId],
                    entityKey = entityKeyById(row[ModelStorageSQLite.Companion.EntityAttributeTable.entityId]),
                    entityLabel = entityLabelById(row[ModelStorageSQLite.Companion.EntityAttributeTable.entityId]),
                    relationshipId = null,
                    relationshipKey = null,
                    relationshipLabel = null,
                    attributeId = attributeId.asString(),
                    attributeKey = row[ModelStorageSQLite.Companion.EntityAttributeTable.key],
                    attributeLabel = entityAttributeLabelFromRow(row),
                    searchText = buildSearchText(
                        row[ModelStorageSQLite.Companion.EntityAttributeTable.key],
                        row[ModelStorageSQLite.Companion.EntityAttributeTable.name],
                        row[ModelStorageSQLite.Companion.EntityAttributeTable.description]
                    )
                ),
                loadTagIds(
                    ModelStorageSQLite.Companion.EntityAttributeTagTable,
                    ModelStorageSQLite.Companion.EntityAttributeTagTable.attributeId,
                    attributeId.asString(),
                    ModelStorageSQLite.Companion.EntityAttributeTagTable.tagId
                )
            )
        }

        private fun upsertRelationshipSearchItemRow(relationshipId: RelationshipId) {
            val row = ModelStorageSQLite.Companion.RelationshipTable.selectAll()
                .where { ModelStorageSQLite.Companion.RelationshipTable.id eq relationshipId.asString() }
                .singleOrNull()
            if (row == null) {
                deleteSearchItemById(searchItemIdForRelationship(relationshipId))
                return
            }

            replaceSearchItem(
                DenormModelSearchItem(
                    id = searchItemIdForRelationship(relationshipId),
                    itemType = SearchItemType.RELATIONSHIP.code,
                    modelId = row[ModelStorageSQLite.Companion.RelationshipTable.modelId],
                    modelKey = modelKeyById(row[ModelStorageSQLite.Companion.RelationshipTable.modelId]),
                    modelLabel = modelLabelById(row[ModelStorageSQLite.Companion.RelationshipTable.modelId]),
                    entityId = null,
                    entityKey = null,
                    entityLabel = null,
                    relationshipId = relationshipId.asString(),
                    relationshipKey = row[ModelStorageSQLite.Companion.RelationshipTable.key],
                    relationshipLabel = relationshipLabelFromRow(row),
                    attributeId = null,
                    attributeKey = null,
                    attributeLabel = null,
                    searchText = buildSearchText(
                        row[ModelStorageSQLite.Companion.RelationshipTable.key],
                        row[ModelStorageSQLite.Companion.RelationshipTable.name],
                        row[ModelStorageSQLite.Companion.RelationshipTable.description]
                    )
                ),
                loadTagIds(
                    ModelStorageSQLite.Companion.RelationshipTagTable,
                    ModelStorageSQLite.Companion.RelationshipTagTable.relationshipId,
                    relationshipId.asString(),
                    ModelStorageSQLite.Companion.RelationshipTagTable.tagId
                )
            )
        }

        private fun upsertRelationshipAttributeSearchItemRow(attributeId: AttributeId) {
            val row = ModelStorageSQLite.Companion.RelationshipAttributeTable.selectAll()
                .where { ModelStorageSQLite.Companion.RelationshipAttributeTable.id eq attributeId.asString() }
                .singleOrNull()
            if (row == null) {
                deleteSearchItemById(searchItemIdForRelationshipAttribute(attributeId))
                return
            }

            val relationshipRow = ModelStorageSQLite.Companion.RelationshipTable.select(ModelStorageSQLite.Companion.RelationshipTable.modelId)
                .where { ModelStorageSQLite.Companion.RelationshipTable.id eq row[ModelStorageSQLite.Companion.RelationshipAttributeTable.relationshipId] }
                .singleOrNull()
                ?: throw ModelStorageSearchSQLiteMissingSourceRowException("relationship", row[ModelStorageSQLite.Companion.RelationshipAttributeTable.relationshipId])

            replaceSearchItem(
                DenormModelSearchItem(
                    id = searchItemIdForRelationshipAttribute(attributeId),
                    itemType = SearchItemType.RELATIONSHIP_ATTRIBUTE.code,
                    modelId = relationshipRow[ModelStorageSQLite.Companion.RelationshipTable.modelId],
                    modelKey = modelKeyById(relationshipRow[ModelStorageSQLite.Companion.RelationshipTable.modelId]),
                    modelLabel = modelLabelById(relationshipRow[ModelStorageSQLite.Companion.RelationshipTable.modelId]),
                    entityId = null,
                    entityKey = null,
                    entityLabel = null,
                    relationshipId = row[ModelStorageSQLite.Companion.RelationshipAttributeTable.relationshipId],
                    relationshipKey = relationshipKeyById(row[ModelStorageSQLite.Companion.RelationshipAttributeTable.relationshipId]),
                    relationshipLabel = relationshipLabelById(row[ModelStorageSQLite.Companion.RelationshipAttributeTable.relationshipId]),
                    attributeId = attributeId.asString(),
                    attributeKey = row[ModelStorageSQLite.Companion.RelationshipAttributeTable.key],
                    attributeLabel = relationshipAttributeLabelFromRow(row),
                    searchText = buildSearchText(
                        row[ModelStorageSQLite.Companion.RelationshipAttributeTable.key],
                        row[ModelStorageSQLite.Companion.RelationshipAttributeTable.name],
                        row[ModelStorageSQLite.Companion.RelationshipAttributeTable.description]
                    )
                ),
                loadTagIds(
                    ModelStorageSQLite.Companion.RelationshipAttributeTagTable,
                    ModelStorageSQLite.Companion.RelationshipAttributeTagTable.attributeId,
                    attributeId.asString(),
                    ModelStorageSQLite.Companion.RelationshipAttributeTagTable.tagId
                )
            )
        }

        private fun replaceSearchItem(item: DenormModelSearchItem, tagIds: List<String>) {
            deleteSearchItemById(item.id)
            ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.insert { row ->
                row[id] = item.id
                row[itemType] = item.itemType
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
                ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.insert { row ->
                    row[searchItemId] = item.id
                    row[ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.tagId] = tagId
                }
            }
        }

        private fun deleteRowsByModelId(modelId: ModelId) {
            ModelStorageSearchSQLiteTables.DenormModelSearchItemTable
                .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id)
                .where { ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.modelId eq modelId.asString() }
                .map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id] }
                .forEach { deleteSearchItemById(it) }
        }

        private fun deleteRowsByEntityId(entityId: EntityId) {
            ModelStorageSearchSQLiteTables.DenormModelSearchItemTable
                .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id)
                .where {
                    (ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.entityId eq entityId.asString()) or
                            ((ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.itemType eq SearchItemType.ENTITY_ATTRIBUTE.code) and
                                    (ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.entityId eq entityId.asString()))
                }
                .map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id] }
                .forEach { deleteSearchItemById(it) }
        }

        private fun deleteRowsByRelationshipId(relationshipId: RelationshipId) {
            ModelStorageSearchSQLiteTables.DenormModelSearchItemTable
                .select(ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id)
                .where {
                    (ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.relationshipId eq relationshipId.asString()) or
                            ((ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.itemType eq SearchItemType.RELATIONSHIP_ATTRIBUTE.code) and
                                    (ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.relationshipId eq relationshipId.asString()))
                }
                .map { it[ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id] }
                .forEach { deleteSearchItemById(it) }
        }

        private fun deleteSearchItemById(searchItemId: String) {
            ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.deleteWhere {
                ModelStorageSearchSQLiteTables.DenormModelSearchItemTagTable.searchItemId eq searchItemId
            }
            ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.deleteWhere {
                ModelStorageSearchSQLiteTables.DenormModelSearchItemTable.id eq searchItemId
            }
        }

        private fun buildSearchText(key: String, name: String?, description: String?): String {
            return normalizeModelSearchText(listOfNotNull(key, name, description).joinToString(" "))
        }

        private fun modelLabelFromRow(row: ResultRow): String {
            return row[ModelStorageSQLite.Companion.ModelTable.name] ?: row[ModelStorageSQLite.Companion.ModelTable.key]
        }

        private fun entityLabelFromRow(row: ResultRow): String {
            return row[ModelStorageSQLite.Companion.EntityTable.name] ?: row[ModelStorageSQLite.Companion.EntityTable.key]
        }

        private fun entityAttributeLabelFromRow(row: ResultRow): String {
            return row[ModelStorageSQLite.Companion.EntityAttributeTable.name] ?: row[ModelStorageSQLite.Companion.EntityAttributeTable.key]
        }

        private fun relationshipLabelFromRow(row: ResultRow): String {
            return row[ModelStorageSQLite.Companion.RelationshipTable.name] ?: row[ModelStorageSQLite.Companion.RelationshipTable.key]
        }

        private fun relationshipAttributeLabelFromRow(row: ResultRow): String {
            return row[ModelStorageSQLite.Companion.RelationshipAttributeTable.name] ?: row[ModelStorageSQLite.Companion.RelationshipAttributeTable.key]
        }

        private fun modelKeyById(modelId: String): String {
            return ModelStorageSQLite.Companion.ModelTable.select(ModelStorageSQLite.Companion.ModelTable.key)
                .where { ModelStorageSQLite.Companion.ModelTable.id eq modelId }
                .single()[ModelStorageSQLite.Companion.ModelTable.key]
        }

        private fun modelLabelById(modelId: String): String {
            val row = ModelStorageSQLite.Companion.ModelTable.select(ModelStorageSQLite.Companion.ModelTable.key, ModelStorageSQLite.Companion.ModelTable.name)
                .where { ModelStorageSQLite.Companion.ModelTable.id eq modelId }
                .single()
            return row[ModelStorageSQLite.Companion.ModelTable.name] ?: row[ModelStorageSQLite.Companion.ModelTable.key]
        }

        private fun entityKeyById(entityId: String): String {
            return ModelStorageSQLite.Companion.EntityTable.select(ModelStorageSQLite.Companion.EntityTable.key)
                .where { ModelStorageSQLite.Companion.EntityTable.id eq entityId }
                .single()[ModelStorageSQLite.Companion.EntityTable.key]
        }

        private fun entityLabelById(entityId: String): String {
            val row = ModelStorageSQLite.Companion.EntityTable.select(ModelStorageSQLite.Companion.EntityTable.key, ModelStorageSQLite.Companion.EntityTable.name)
                .where { ModelStorageSQLite.Companion.EntityTable.id eq entityId }
                .single()
            return row[ModelStorageSQLite.Companion.EntityTable.name] ?: row[ModelStorageSQLite.Companion.EntityTable.key]
        }

        private fun relationshipKeyById(relationshipId: String): String {
            return ModelStorageSQLite.Companion.RelationshipTable.select(ModelStorageSQLite.Companion.RelationshipTable.key)
                .where { ModelStorageSQLite.Companion.RelationshipTable.id eq relationshipId }
                .single()[ModelStorageSQLite.Companion.RelationshipTable.key]
        }

        private fun relationshipLabelById(relationshipId: String): String {
            val row = ModelStorageSQLite.Companion.RelationshipTable.select(ModelStorageSQLite.Companion.RelationshipTable.key, ModelStorageSQLite.Companion.RelationshipTable.name)
                .where { ModelStorageSQLite.Companion.RelationshipTable.id eq relationshipId }
                .single()
            return row[ModelStorageSQLite.Companion.RelationshipTable.name] ?: row[ModelStorageSQLite.Companion.RelationshipTable.key]
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