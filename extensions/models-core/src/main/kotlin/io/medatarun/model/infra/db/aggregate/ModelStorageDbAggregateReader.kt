package io.medatarun.model.infra.db.aggregate

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.infra.AttributeInMemory
import io.medatarun.model.infra.EntityInMemory
import io.medatarun.model.infra.ModelAggregateInMemory
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.infra.RelationshipInMemory
import io.medatarun.model.infra.db.ModelStorageAdapters.toEntity
import io.medatarun.model.infra.db.ModelStorageAdapters.toEntityAttribute
import io.medatarun.model.infra.db.ModelStorageAdapters.toModel
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationship
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationshipAttribute
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationshipRole
import io.medatarun.model.infra.db.ModelStorageAdapters.toType
import io.medatarun.model.infra.db.records.EntityAttributeRecord
import io.medatarun.model.infra.db.records.EntityRecord
import io.medatarun.model.infra.db.records.ModelRecord
import io.medatarun.model.infra.db.records.ModelTypeRecord
import io.medatarun.model.infra.db.records.RelationshipAttributeRecord
import io.medatarun.model.infra.db.records.RelationshipRecord
import io.medatarun.model.infra.db.records.RelationshipRoleRecord
import io.medatarun.model.infra.db.tables.EntityAttributeTable
import io.medatarun.model.infra.db.tables.EntityAttributeTagTable
import io.medatarun.model.infra.db.tables.EntityTable
import io.medatarun.model.infra.db.tables.EntityTagTable
import io.medatarun.model.infra.db.tables.ModelTagTable
import io.medatarun.model.infra.db.tables.ModelTypeTable
import io.medatarun.model.infra.db.tables.RelationshipAttributeTable
import io.medatarun.model.infra.db.tables.RelationshipAttributeTagTable
import io.medatarun.model.infra.db.tables.RelationshipRoleTable
import io.medatarun.model.infra.db.tables.RelationshipTable
import io.medatarun.model.infra.db.tables.RelationshipTagTable
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.collections.map

class ModelStorageDbAggregateReader(
    private val snapshots: ModelStorageDbSnapshots
) {

    fun loadModelAggregate(row: ResultRow): ModelAggregateInMemory {
        val record = ModelRecord.read(row)
        val types = loadTypes(record.modelId)
        val entities = loadEntities(record.modelId)
        val entityAttributes = loadEntityAttributes(record.modelId)
        val relationships = loadRelationships(record.modelId)
        val relationshipAttributes = loadRelationshipAttributes(record.modelId)

        return ModelAggregateInMemory(
            model = toModel(record),
            types = types,
            entities = entities,
            relationships = relationships,
            tags = loadModelTags(record.snapshotId),
            attributes = entityAttributes + relationshipAttributes
        )
    }

    private fun loadModelTags(modelSnapshotId: String): List<TagId> {
        return ModelTagTable.selectAll().where { ModelTagTable.modelSnapshotId eq ModelId.fromString(modelSnapshotId) }
            .orderBy(ModelTagTable.tagId to SortOrder.ASC).map { it[ModelTagTable.tagId] }
    }

    private fun loadTypes(modelId: ModelId): List<ModelTypeInMemory> {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        return ModelTypeTable.selectAll().where { ModelTypeTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(ModelTypeTable.key to SortOrder.ASC).map { row ->
                toType(ModelTypeRecord.read(row))
            }
    }
    private fun loadEntities(modelId: ModelId): List<EntityInMemory> {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val identifierAttributeTable = EntityAttributeTable.alias("identifier_attribute_snapshot")
        return EntityTable.join(
            identifierAttributeTable,
            JoinType.INNER,
            onColumn = EntityTable.identifierAttributeSnapshotId,
            otherColumn = identifierAttributeTable[EntityAttributeTable.id]
        ).selectAll().where { EntityTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(EntityTable.key to SortOrder.ASC).map { row ->
                val record = EntityRecord.read(row)
                val tags = loadEntityTags(record.snapshotId)
                toEntity(record, tags, row[identifierAttributeTable[EntityAttributeTable.lineageId]])
            }
    }
    fun loadEntityTags(entityId: EntityId): List<TagId> {
        return EntityTagTable.selectAll().where { EntityTagTable.entitySnapshotId eq entityId }
            .orderBy(EntityTagTable.tagId to SortOrder.ASC).map { it[EntityTagTable.tagId] }
    }
    private fun loadEntityAttributes(modelId: ModelId): List<AttributeInMemory> {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val typeTable = ModelTypeTable.alias("entity_attribute_type_snapshot")
        return EntityAttributeTable.join(
            EntityTable,
            joinType = JoinType.INNER,
            onColumn = EntityAttributeTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).join(
            typeTable,
            joinType = JoinType.INNER,
            onColumn = EntityAttributeTable.typeSnapshotId,
            otherColumn = typeTable[ModelTypeTable.id]
        ).selectAll().where { EntityTable.modelSnapshotId eq modelSnapshotId }.map { row ->
            val record = EntityAttributeRecord.read(row)
            val tags = loadEntityAttributeTags(record.snapshotId)
            toEntityAttribute(
                record,
                tags,
                row[typeTable[ModelTypeTable.lineageId]],
                row[EntityTable.lineageId]
            )
        }
    }

    fun loadEntityAttributeTags(attributeId: AttributeId): List<TagId> {
        return EntityAttributeTagTable.selectAll().where { EntityAttributeTagTable.attributeSnapshotId eq attributeId }
            .orderBy(EntityAttributeTagTable.tagId to SortOrder.ASC).map { it[EntityAttributeTagTable.tagId] }
    }
    private fun loadRelationships(modelId: ModelId): List<RelationshipInMemory> {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val relationshipIds =
            RelationshipTable.select(RelationshipTable.id)
                .where { RelationshipTable.modelSnapshotId eq modelSnapshotId }
        val roleEntityTable = EntityTable.alias("relationship_role_entity_snapshot")

        val roleRowsByRelationshipId =
            RelationshipRoleTable.join(
                roleEntityTable,
                JoinType.INNER,
                onColumn = RelationshipRoleTable.entitySnapshotId,
                otherColumn = roleEntityTable[EntityTable.id]
            ).selectAll().where { RelationshipRoleTable.relationshipSnapshotId inSubQuery relationshipIds }
                .orderBy(RelationshipRoleTable.key to SortOrder.ASC).toList()
                .groupBy { it[RelationshipRoleTable.relationshipSnapshotId] }

        return RelationshipTable.selectAll().where { RelationshipTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(RelationshipTable.key to SortOrder.ASC).map { row ->
                val relationshipRecord = RelationshipRecord.read(row)
                val relationshipId = relationshipRecord.snapshotId
                val roles = (roleRowsByRelationshipId[relationshipId] ?: emptyList()).map { roleRow ->
                    toRelationshipRole(
                        RelationshipRoleRecord.read(roleRow),
                        roleRow[roleEntityTable[EntityTable.lineageId]]
                    )
                }
                val tags = loadRelationshipTags(relationshipRecord.snapshotId)
                toRelationship(relationshipRecord, roles, tags)
            }
    }

    fun loadRelationshipTags(relationshipId: RelationshipId): List<TagId> {
        return RelationshipTagTable.selectAll().where { RelationshipTagTable.relationshipSnapshotId eq relationshipId }
            .orderBy(RelationshipTagTable.tagId to SortOrder.ASC).map { it[RelationshipTagTable.tagId] }
    }

    private fun loadRelationshipAttributes(modelId: ModelId): List<AttributeInMemory> {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val typeTable = ModelTypeTable.alias("relationship_attribute_type_snapshot")
        return RelationshipTable.join(
            RelationshipAttributeTable,
            joinType = JoinType.INNER,
            onColumn = RelationshipTable.id,
            otherColumn = RelationshipAttributeTable.relationshipSnapshotId
        ).join(
            typeTable,
            joinType = JoinType.INNER,
            onColumn = RelationshipAttributeTable.typeSnapshotId,
            otherColumn = typeTable[ModelTypeTable.id]
        ).selectAll().where { RelationshipTable.modelSnapshotId eq modelSnapshotId }.map { row ->
            val record = RelationshipAttributeRecord.read(row)
            val tags = loadRelationshipAttributeTags(record.snapshotId)
            toRelationshipAttribute(
                record,
                tags,
                row[typeTable[ModelTypeTable.lineageId]],
                row[RelationshipTable.lineageId]
            )
        }
    }

    fun loadRelationshipAttributeTags(attributeId: AttributeId): List<TagId> {
        return RelationshipAttributeTagTable.selectAll()
            .where { RelationshipAttributeTagTable.attributeSnapshotId eq attributeId }
            .orderBy(RelationshipAttributeTagTable.tagId to SortOrder.ASC)
            .map { it[RelationshipAttributeTagTable.tagId] }
    }

}