package io.medatarun.model.infra.db.aggregate

import io.medatarun.model.domain.AttributeSnapshotId
import io.medatarun.model.domain.BusinessKeySnapshotId
import io.medatarun.model.domain.EntitySnapshotId
import io.medatarun.model.domain.EntityPKSnapshotId
import io.medatarun.model.domain.ModelSnapshotId
import io.medatarun.model.domain.RelationshipSnapshotId
import io.medatarun.model.infra.*
import io.medatarun.model.infra.db.ModelStorageAdapters.toEntity
import io.medatarun.model.infra.db.ModelStorageAdapters.toEntityAttribute
import io.medatarun.model.infra.db.ModelStorageAdapters.toModel
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationship
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationshipAttribute
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationshipRole
import io.medatarun.model.infra.db.ModelStorageAdapters.toType
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.snapshots.SnapshotSelector
import io.medatarun.model.infra.db.tables.*
import io.medatarun.model.infra.inmemory.BusinessKeyInMemory
import io.medatarun.model.infra.inmemory.EntityPrimaryKeyInMemory
import io.medatarun.model.infra.inmemory.PBKeyParticipantInMemory
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

class ModelStorageDbAggregateReader {

    fun loadModelAggregateOptional(snapshotSelector: SnapshotSelector): ModelAggregateInMemory? {
        val row = ModelSnapshotTable.selectAll()
            .where { snapshotSelector.criterion() }
            .singleOrNull()
            ?: return null
        val record = ModelRecord.read(row)
        val modelSnapshotId = record.snapshotId
        val types = loadTypes(modelSnapshotId)
        val entities = loadEntities(modelSnapshotId)
        val entityAttributes = loadEntityAttributes(modelSnapshotId)
        val relationships = loadRelationships(modelSnapshotId)
        val relationshipAttributes = loadRelationshipAttributes(modelSnapshotId)
        val entityPrimaryKeys = loadEntityPrimaryKeys(modelSnapshotId)
        val businessKeys = loadBusinessKeys(modelSnapshotId)

        return ModelAggregateInMemory(
            model = toModel(record),
            types = types,
            entities = entities,
            relationships = relationships,
            tags = loadModelTags(record.snapshotId),
            attributes = entityAttributes + relationshipAttributes,
            entityPrimaryKeys = entityPrimaryKeys,
            businessKeys = businessKeys
        )
    }

    private fun loadModelTags(modelSnapshotId: ModelSnapshotId): List<TagId> {
        return ModelTagTable.selectAll().where { ModelTagTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(ModelTagTable.tagId to SortOrder.ASC).map { it[ModelTagTable.tagId] }
    }

    private fun loadTypes(modelSnapshotId: ModelSnapshotId): List<ModelTypeInMemory> {
        return ModelTypeTable.selectAll().where { ModelTypeTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(ModelTypeTable.key to SortOrder.ASC).map { row ->
                toType(ModelTypeRecord.read(row))
            }
    }

    private fun loadEntities(modelSnapshotId: ModelSnapshotId): List<EntityInMemory> {
        val identifierAttributeTable = EntityAttributeTable.alias("identifier_attribute_snapshot")
        val rows = EntityTable.join(
            identifierAttributeTable,
            JoinType.INNER,
            onColumn = EntityTable.identifierAttributeSnapshotId,
            otherColumn = identifierAttributeTable[EntityAttributeTable.id]
        ).selectAll().where { EntityTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(EntityTable.key to SortOrder.ASC)
            .toList()
        val entityTagsBySnapshotId = loadEntityTagsByModelSnapshotId(modelSnapshotId)
        return rows.map { row ->
            val record = EntityRecord.read(row)
            toEntity(
                record,
                entityTagsBySnapshotId[record.snapshotId] ?: emptyList(),
                row[identifierAttributeTable[EntityAttributeTable.lineageId]]
            )
        }
    }

    private fun loadEntityTagsByModelSnapshotId(modelSnapshotId: ModelSnapshotId): Map<EntitySnapshotId, List<TagId>> {
        return EntityTable.join(
            EntityTagTable,
            JoinType.INNER,
            onColumn = EntityTable.id,
            otherColumn = EntityTagTable.entitySnapshotId
        ).selectAll()
            .where { EntityTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(EntityTagTable.tagId to SortOrder.ASC)
            .groupBy { row -> row[EntityTagTable.entitySnapshotId] }
            .mapValues { entry ->
                entry.value.map { row -> row[EntityTagTable.tagId] }
            }
    }

    private fun loadEntityAttributes(modelSnapshotId: ModelSnapshotId): List<AttributeInMemory> {
        val typeTable = ModelTypeTable.alias("entity_attribute_type_snapshot")
        val rows = EntityAttributeTable.join(
            EntityTable,
            joinType = JoinType.INNER,
            onColumn = EntityAttributeTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).join(
            typeTable,
            joinType = JoinType.INNER,
            onColumn = EntityAttributeTable.typeSnapshotId,
            otherColumn = typeTable[ModelTypeTable.id]
        ).selectAll().where { EntityTable.modelSnapshotId eq modelSnapshotId }.toList()
        val attributeTagsBySnapshotId = loadEntityAttributeTagsByModelSnapshotId(modelSnapshotId)
        return rows.map { row ->
            val record = EntityAttributeRecord.read(row)
            toEntityAttribute(
                record,
                attributeTagsBySnapshotId[record.snapshotId] ?: emptyList(),
                row[typeTable[ModelTypeTable.lineageId]],
                row[EntityTable.lineageId]
            )
        }
    }

    private fun loadEntityAttributeTagsByModelSnapshotId(modelSnapshotId: ModelSnapshotId): Map<AttributeSnapshotId, List<TagId>> {
        return EntityTable.join(
            EntityAttributeTable,
            JoinType.INNER,
            onColumn = EntityTable.id,
            otherColumn = EntityAttributeTable.entitySnapshotId
        ).join(
            EntityAttributeTagTable,
            JoinType.INNER,
            onColumn = EntityAttributeTable.id,
            otherColumn = EntityAttributeTagTable.attributeSnapshotId
        ).selectAll()
            .where { EntityTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(EntityAttributeTagTable.tagId to SortOrder.ASC)
            .groupBy { row -> row[EntityAttributeTagTable.attributeSnapshotId] }
            .mapValues { entry ->
                entry.value.map { row -> row[EntityAttributeTagTable.tagId] }
            }
    }

    private fun loadRelationships(modelSnapshotId: ModelSnapshotId): List<RelationshipInMemory> {
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

        val relationshipRows = RelationshipTable.selectAll()
            .where { RelationshipTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(RelationshipTable.key to SortOrder.ASC)
            .toList()
        val relationshipTagsBySnapshotId = loadRelationshipTagsByModelSnapshotId(modelSnapshotId)

        return relationshipRows.map { row ->
            val relationshipRecord = RelationshipRecord.read(row)
            val relationshipId = relationshipRecord.snapshotId
            val roles = (roleRowsByRelationshipId[relationshipId] ?: emptyList()).map { roleRow ->
                toRelationshipRole(
                    RelationshipRoleRecord.read(roleRow),
                    roleRow[roleEntityTable[EntityTable.lineageId]]
                )
            }
            toRelationship(
                relationshipRecord,
                roles,
                relationshipTagsBySnapshotId[relationshipRecord.snapshotId] ?: emptyList()
            )
        }
    }

    private fun loadRelationshipTagsByModelSnapshotId(modelSnapshotId: ModelSnapshotId): Map<RelationshipSnapshotId, List<TagId>> {
        return RelationshipTable.join(
            RelationshipTagTable,
            JoinType.INNER,
            onColumn = RelationshipTable.id,
            otherColumn = RelationshipTagTable.relationshipSnapshotId
        ).selectAll()
            .where { RelationshipTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(RelationshipTagTable.tagId to SortOrder.ASC)
            .groupBy { row -> row[RelationshipTagTable.relationshipSnapshotId] }
            .mapValues { entry ->
                entry.value.map { row -> row[RelationshipTagTable.tagId] }
            }
    }

    private fun loadRelationshipAttributes(modelSnapshotId: ModelSnapshotId): List<AttributeInMemory> {
        val typeTable = ModelTypeTable.alias("relationship_attribute_type_snapshot")
        val rows = RelationshipTable.join(
            RelationshipAttributeTable,
            joinType = JoinType.INNER,
            onColumn = RelationshipTable.id,
            otherColumn = RelationshipAttributeTable.relationshipSnapshotId
        ).join(
            typeTable,
            joinType = JoinType.INNER,
            onColumn = RelationshipAttributeTable.typeSnapshotId,
            otherColumn = typeTable[ModelTypeTable.id]
        ).selectAll().where { RelationshipTable.modelSnapshotId eq modelSnapshotId }.toList()
        val attributeTagsBySnapshotId = loadRelationshipAttributeTagsByModelSnapshotId(modelSnapshotId)
        return rows.map { row ->
            val record = RelationshipAttributeRecord.read(row)
            toRelationshipAttribute(
                record,
                attributeTagsBySnapshotId[record.snapshotId] ?: emptyList(),
                row[typeTable[ModelTypeTable.lineageId]],
                row[RelationshipTable.lineageId]
            )
        }
    }

    private fun loadRelationshipAttributeTagsByModelSnapshotId(modelSnapshotId: ModelSnapshotId): Map<AttributeSnapshotId, List<TagId>> {
        return RelationshipTable.join(
            RelationshipAttributeTable,
            JoinType.INNER,
            onColumn = RelationshipTable.id,
            otherColumn = RelationshipAttributeTable.relationshipSnapshotId
        ).join(
            RelationshipAttributeTagTable,
            JoinType.INNER,
            onColumn = RelationshipAttributeTable.id,
            otherColumn = RelationshipAttributeTagTable.attributeSnapshotId
        ).selectAll()
            .where { RelationshipTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(RelationshipAttributeTagTable.tagId to SortOrder.ASC)
            .groupBy { row -> row[RelationshipAttributeTagTable.attributeSnapshotId] }
            .mapValues { entry ->
                entry.value.map { row -> row[RelationshipAttributeTagTable.tagId] }
            }
    }

    private fun loadEntityPrimaryKeys(modelSnapshotId: ModelSnapshotId): List<EntityPrimaryKeyInMemory> {
        val rows = EntityPKTable.join(
            EntityTable,
            JoinType.INNER,
            onColumn = EntityPKTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).selectAll().where { EntityTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(EntityPKTable.id to SortOrder.ASC)
            .toList()
        if (rows.isEmpty()) {
            return emptyList()
        }
        val participantsByPrimaryKey = loadEntityPrimaryKeyParticipants(rows.map { row -> row[EntityPKTable.id] })
        return rows.map { row ->
            val participants = participantsByPrimaryKey[row[EntityPKTable.id]] ?: emptyList()
            EntityPrimaryKeyInMemory(
                id = row[EntityPKTable.lineageId],
                entityId = row[EntityTable.lineageId],
                participants = participants
            )
        }
    }

    /**
     * Primary key participants reference attribute snapshots, while aggregate expects lineage attribute ids.
     */
    private fun loadEntityPrimaryKeyParticipants(
        primaryKeySnapshotIds: List<EntityPKSnapshotId>
    ): Map<EntityPKSnapshotId, List<PBKeyParticipantInMemory>> {
        if (primaryKeySnapshotIds.isEmpty()) {
            return emptyMap()
        }
        return EntityPKAttributeTable.join(
            EntityAttributeTable,
            JoinType.INNER,
            onColumn = EntityPKAttributeTable.attributeSnapshotId,
            otherColumn = EntityAttributeTable.id
        ).selectAll().where {
            EntityPKAttributeTable.entityPKSnapshotId inList primaryKeySnapshotIds
        }.orderBy(
            EntityPKAttributeTable.entityPKSnapshotId to SortOrder.ASC,
            EntityPKAttributeTable.priority to SortOrder.ASC
        ).groupBy { row ->
            row[EntityPKAttributeTable.entityPKSnapshotId]
        }.mapValues { entry ->
            entry.value.map { row ->
                PBKeyParticipantInMemory(
                    attributeId = row[EntityAttributeTable.lineageId],
                    position = row[EntityPKAttributeTable.priority]
                )
            }
        }
    }

    private fun loadBusinessKeys(modelSnapshotId: ModelSnapshotId): List<BusinessKeyInMemory> {
        val rows = BusinessKeyTable.join(
            EntityTable,
            JoinType.INNER,
            onColumn = BusinessKeyTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).selectAll().where { EntityTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(BusinessKeyTable.id to SortOrder.ASC)
            .toList()
        if (rows.isEmpty()) {
            return emptyList()
        }
        val participantsByBusinessKey = loadBusinessKeyParticipants(rows.map { row -> row[BusinessKeyTable.id] })
        return rows.map { row ->
            val participants = participantsByBusinessKey[row[BusinessKeyTable.id]] ?: emptyList()
            BusinessKeyInMemory(
                id = row[BusinessKeyTable.lineageId],
                key = row[BusinessKeyTable.key],
                entityId = row[EntityTable.lineageId],
                name = row[BusinessKeyTable.name],
                description = row[BusinessKeyTable.description],
                participants = participants
            )
        }
    }

    /**
     * Business key participants reference attribute snapshots, while aggregate expects lineage attribute ids.
     */
    private fun loadBusinessKeyParticipants(
        businessKeySnapshotIds: List<BusinessKeySnapshotId>
    ): Map<BusinessKeySnapshotId, List<PBKeyParticipantInMemory>> {
        if (businessKeySnapshotIds.isEmpty()) {
            return emptyMap()
        }
        return BusinessKeyAttributeTable.join(
            EntityAttributeTable,
            JoinType.INNER,
            onColumn = BusinessKeyAttributeTable.attributeSnapshotId,
            otherColumn = EntityAttributeTable.id
        ).selectAll().where {
            BusinessKeyAttributeTable.businessKeySnapshotId inList businessKeySnapshotIds
        }.orderBy(
            BusinessKeyAttributeTable.businessKeySnapshotId to SortOrder.ASC,
            BusinessKeyAttributeTable.priority to SortOrder.ASC
        ).groupBy { row ->
            row[BusinessKeyAttributeTable.businessKeySnapshotId]
        }.mapValues { entry ->
            entry.value.map { row ->
                PBKeyParticipantInMemory(
                    attributeId = row[EntityAttributeTable.lineageId],
                    position = row[BusinessKeyAttributeTable.priority]
                )
            }
        }
    }

}
