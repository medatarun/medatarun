package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.ModelStorageAdapters.toEntity
import io.medatarun.model.infra.db.ModelStorageAdapters.toEntityAttribute
import io.medatarun.model.infra.db.ModelStorageAdapters.toModel
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationship
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationshipAttribute
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationshipRole
import io.medatarun.model.infra.db.ModelStorageAdapters.toType
import io.medatarun.model.infra.db.aggregate.ModelStorageDbAggregateReader
import io.medatarun.model.infra.db.snapshots.ModelStorageDbSnapshots
import io.medatarun.model.infra.db.events.ModelEventRegistry
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.tables.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

class ModelStorageDbRead(
    private val snapshots: ModelStorageDbSnapshots,
    private val modelEventRegistry: ModelEventRegistry
) {

    private val aggregateReader = ModelStorageDbAggregateReader( snapshots)

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    // Model

    fun existsModelById(id: ModelId): Boolean {
        return ModelTable.select(ModelTable.id).where { ModelTable.id eq id }.limit(1).any()
    }

    fun existsModelByKey(key: ModelKey): Boolean {
        return ModelSnapshotTable.select(ModelSnapshotTable.id).where {
            (ModelSnapshotTable.snapshotKind eq snapshots.CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.key eq key)
        }.limit(1).any()
    }

    fun findAllModelIds(): List<ModelId> {
        return ModelTable.selectAll().map { it[ModelTable.id] }
    }

    fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate? {
        val row = ModelSnapshotTable.selectAll().where {
            (ModelSnapshotTable.snapshotKind eq snapshots.CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.key eq key)
        }.singleOrNull()
        return if (row == null) null else aggregateReader.loadModelAggregate(row)
    }

    fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate? {
        val row = ModelSnapshotTable.selectAll().where {
            (ModelSnapshotTable.snapshotKind eq snapshots.CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.modelId eq id)
        }.singleOrNull()
        return if (row == null) null else aggregateReader.loadModelAggregate(row)
    }

    fun findAllModelEvents(modelId: ModelId): List<ModelEventRecord> {
        return ModelEventTable.selectAll()
            .where { ModelEventTable.modelId eq modelId }
            .orderBy(ModelEventTable.streamRevision to SortOrder.ASC)
            .map(ModelEventRecord::read)
    }

    fun findModelByKeyOptional(key: ModelKey): Model? {
        val row = ModelSnapshotTable.selectAll().where {
            (ModelSnapshotTable.snapshotKind eq snapshots.CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.key eq key)
        }.singleOrNull()
        return if (row == null) null else toModel(ModelRecord.read(row))
    }

    fun findModelByIdOptional(id: ModelId): Model? {
        val row = ModelSnapshotTable.selectAll().where {
            (ModelSnapshotTable.snapshotKind eq snapshots.CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.modelId eq id)
        }.singleOrNull()
        return if (row == null) null else toModel(ModelRecord.read(row))
    }

    fun findLatestModelReleaseVersionOptional(modelId: ModelId): ModelVersion? {
        return ModelEventTable.select(ModelEventTable.modelVersion)
            .where {
                (ModelEventTable.modelId eq modelId) and
                    (ModelEventTable.eventType eq modelEventRegistry.modelReleaseEventType())
            }
            .orderBy(ModelEventTable.streamRevision to SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.let { row ->
                val modelVersion = row[ModelEventTable.modelVersion]
                    ?: throw ModelStorageDbInvalidReleaseEventException(modelId, row[ModelEventTable.id])
                ModelVersion(modelVersion)
            }
    }

    fun findTypeByKeyOptional(
        modelId: ModelId, key: TypeKey
    ): ModelType? {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        return ModelTypeTable.selectAll().where {
            (ModelTypeTable.modelSnapshotId eq modelSnapshotId) and (ModelTypeTable.key eq key)
        }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
    }

    fun findTypeByIdOptional(
        modelId: ModelId, typeId: TypeId
    ): ModelType? {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        return ModelTypeTable.selectAll().where {
            (ModelTypeTable.modelSnapshotId eq modelSnapshotId) and (ModelTypeTable.lineageId eq typeId)
        }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
    }

    fun findEntityByIdOptional(
        modelId: ModelId, entityId: EntityId
    ): Entity? {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val identifierAttributeTable = EntityAttributeTable.alias("identifier_attribute_snapshot")
        val entityTagTable = EntityTagTable.alias("entity_tag_snapshot")
        return EntityTable.join(
            identifierAttributeTable,
            JoinType.INNER,
            onColumn = EntityTable.identifierAttributeSnapshotId,
            otherColumn = identifierAttributeTable[EntityAttributeTable.id]
        ).join(
            entityTagTable,
            JoinType.LEFT,
            onColumn = EntityTable.id,
            otherColumn = entityTagTable[EntityTagTable.entitySnapshotId]
        ).selectAll().where {
            (EntityTable.modelSnapshotId eq modelSnapshotId) and (EntityTable.lineageId eq entityId)
        }.orderBy(entityTagTable[EntityTagTable.tagId] to SortOrder.ASC)
            .toList()
            .let { rows ->
                if (rows.isEmpty()) {
                    null
                } else {
                    val row = rows.first()
                    val record = EntityRecord.read(row)
                    val tags = rows
                        .mapNotNull { tagRow -> readOptionalTagId(tagRow, entityTagTable[EntityTagTable.tagId]) }
                        .distinct()
                    toEntity(
                        record,
                        tags,
                        row[identifierAttributeTable[EntityAttributeTable.lineageId]]
                    )
                }
            }
    }

    fun findEntityByKeyOptional(
        modelId: ModelId, entityKey: EntityKey
    ): Entity? {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val identifierAttributeTable = EntityAttributeTable.alias("identifier_attribute_snapshot")
        val entityTagTable = EntityTagTable.alias("entity_tag_snapshot")
        return EntityTable.join(
            identifierAttributeTable,
            JoinType.INNER,
            onColumn = EntityTable.identifierAttributeSnapshotId,
            otherColumn = identifierAttributeTable[EntityAttributeTable.id]
        ).join(
            entityTagTable,
            JoinType.LEFT,
            onColumn = EntityTable.id,
            otherColumn = entityTagTable[EntityTagTable.entitySnapshotId]
        ).selectAll().where {
            (EntityTable.modelSnapshotId eq modelSnapshotId) and (EntityTable.key eq entityKey)
        }.orderBy(entityTagTable[EntityTagTable.tagId] to SortOrder.ASC)
            .toList()
            .let { rows ->
                if (rows.isEmpty()) {
                    null
                } else {
                    val row = rows.first()
                    val record = EntityRecord.read(row)
                    val tags = rows
                        .mapNotNull { tagRow -> readOptionalTagId(tagRow, entityTagTable[EntityTagTable.tagId]) }
                        .distinct()
                    toEntity(record, tags, row[identifierAttributeTable[EntityAttributeTable.lineageId]])
                }
            }
    }

    private fun readOptionalTagId(row: ResultRow, column: Column<io.medatarun.tags.core.domain.TagId>): io.medatarun.tags.core.domain.TagId? {
        return try {
            row.getOrNull(column)
        } catch (_: IllegalStateException) {
            null
        }
    }

    fun findEntityAttributeByIdOptional(
        modelId: ModelId, entityId: EntityId, attributeId: AttributeId
    ): Attribute? {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val typeTable = ModelTypeTable.alias("entity_attribute_type_snapshot")
        val attributeTagTable = EntityAttributeTagTable.alias("entity_attribute_tag_snapshot")
        return EntityAttributeTable.join(
            EntityTable, JoinType.INNER, EntityAttributeTable.entitySnapshotId, EntityTable.id
        ).join(
            typeTable,
            JoinType.INNER,
            onColumn = EntityAttributeTable.typeSnapshotId,
            otherColumn = typeTable[ModelTypeTable.id]
        ).join(
            attributeTagTable,
            JoinType.LEFT,
            onColumn = EntityAttributeTable.id,
            otherColumn = attributeTagTable[EntityAttributeTagTable.attributeSnapshotId]
        ).selectAll()
            .where { (EntityTable.modelSnapshotId eq modelSnapshotId) and (EntityTable.lineageId eq entityId) and (EntityAttributeTable.lineageId eq attributeId) }
            .orderBy(attributeTagTable[EntityAttributeTagTable.tagId] to SortOrder.ASC)
            .toList()
            .let { rows ->
                if (rows.isEmpty()) {
                    null
                } else {
                    val row = rows.first()
                    val record = EntityAttributeRecord.read(row)
                    val tags = rows
                        .mapNotNull { tagRow -> readOptionalTagId(tagRow, attributeTagTable[EntityAttributeTagTable.tagId]) }
                        .distinct()
                    toEntityAttribute(
                        record,
                        tags,
                        row[typeTable[ModelTypeTable.lineageId]],
                        row[EntityTable.lineageId]
                    )
                }
            }
    }

    fun findEntityAttributeByKeyOptional(
        modelId: ModelId, entityId: EntityId, key: AttributeKey
    ): Attribute? {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val typeTable = ModelTypeTable.alias("entity_attribute_type_snapshot")
        val attributeTagTable = EntityAttributeTagTable.alias("entity_attribute_tag_snapshot")
        return EntityAttributeTable.join(
            EntityTable, JoinType.INNER, EntityAttributeTable.entitySnapshotId, EntityTable.id
        ).join(
            typeTable,
            JoinType.INNER,
            onColumn = EntityAttributeTable.typeSnapshotId,
            otherColumn = typeTable[ModelTypeTable.id]
        ).join(
            attributeTagTable,
            JoinType.LEFT,
            onColumn = EntityAttributeTable.id,
            otherColumn = attributeTagTable[EntityAttributeTagTable.attributeSnapshotId]
        ).selectAll()
            .where { (EntityTable.modelSnapshotId eq modelSnapshotId) and (EntityTable.lineageId eq entityId) and (EntityAttributeTable.key eq key) }
            .orderBy(attributeTagTable[EntityAttributeTagTable.tagId] to SortOrder.ASC)
            .toList()
            .let { rows ->
                if (rows.isEmpty()) {
                    null
                } else {
                    val row = rows.first()
                    val record = EntityAttributeRecord.read(row)
                    val tags = rows
                        .mapNotNull { tagRow -> readOptionalTagId(tagRow, attributeTagTable[EntityAttributeTagTable.tagId]) }
                        .distinct()
                    toEntityAttribute(
                        record,
                        tags,
                        row[typeTable[ModelTypeTable.lineageId]],
                        row[EntityTable.lineageId]
                    )
                }
            }
    }

    fun findRelationshipByIdOptional(modelId: ModelId, relationshipId: RelationshipId): Relationship? {
        return findRelationshipByOptional(modelId, RelationshipTable.lineageId eq relationshipId)
    }

    fun findRelationshipByKeyOptional(modelId: ModelId, relationshipKey: RelationshipKey): Relationship? {
        return findRelationshipByOptional(modelId, RelationshipTable.key eq relationshipKey)
    }


    private fun findRelationshipByOptional(modelId: ModelId, criterion: Expression<Boolean>): Relationship? {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val roleEntityTable = EntityTable.alias("relationship_role_entity_snapshot")
        val relationshipRows = RelationshipTable.join(
            RelationshipRoleTable,
            JoinType.INNER,
            onColumn = RelationshipTable.id,
            otherColumn = RelationshipRoleTable.relationshipSnapshotId
        ).join(
            roleEntityTable,
            JoinType.INNER,
            onColumn = RelationshipRoleTable.entitySnapshotId,
            otherColumn = roleEntityTable[EntityTable.id]
        ).selectAll().where { (RelationshipTable.modelSnapshotId eq modelSnapshotId) and criterion }
            .orderBy(RelationshipRoleTable.key to SortOrder.ASC)
            .toList()

        if (relationshipRows.isEmpty()) {
            return null
        }

        val relationshipRecord = RelationshipRecord.read(relationshipRows.first())
        val roles = relationshipRows
            .distinctBy { it[RelationshipRoleTable.id] }
            .map { row ->
                toRelationshipRole(
                    RelationshipRoleRecord.read(row),
                    row[roleEntityTable[EntityTable.lineageId]]
                )
            }
        val tags = aggregateReader.loadRelationshipTags(relationshipRecord.snapshotId)

        return toRelationship(relationshipRecord, roles, tags)
    }

    fun findRelationshipRoleByIdOptional(
        modelId: ModelId, relationshipId: RelationshipId, roleId: RelationshipRoleId
    ): RelationshipRole? {
        return findRelationshipRoleByOptional(modelId, relationshipId, RelationshipRoleTable.lineageId eq roleId)
    }

    fun findRelationshipRoleByKeyOptional(
        modelId: ModelId, relationshipId: RelationshipId, roleKey: RelationshipRoleKey
    ): RelationshipRole? {
        return findRelationshipRoleByOptional(modelId, relationshipId, RelationshipRoleTable.key eq roleKey)
    }

    private fun findRelationshipRoleByOptional(
        modelId: ModelId, relationshipId: RelationshipId, criterion: Op<Boolean>
    ): RelationshipRole? {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val roleEntityTable = EntityTable.alias("relationship_role_entity_snapshot")
        return RelationshipRoleTable.join(
            RelationshipTable,
            JoinType.INNER,
            onColumn = RelationshipRoleTable.relationshipSnapshotId,
            otherColumn = RelationshipTable.id
        ).join(
            roleEntityTable,
            JoinType.INNER,
            onColumn = RelationshipRoleTable.entitySnapshotId,
            otherColumn = roleEntityTable[EntityTable.id]
        ).selectAll().where {
            (RelationshipTable.modelSnapshotId eq modelSnapshotId) and (RelationshipTable.lineageId eq relationshipId) and criterion
        }.singleOrNull()?.let { row ->
            toRelationshipRole(
                RelationshipRoleRecord.read(row),
                row[roleEntityTable[EntityTable.lineageId]]
            )
        }
    }


    fun findRelationshipAttributeByIdOptional(
        modelId: ModelId, relationshipId: RelationshipId, attributeId: AttributeId
    ): Attribute? {
        return findRelationshipAttributeByOptional(
            modelId, relationshipId, RelationshipAttributeTable.lineageId eq attributeId
        )
    }

    fun findRelationshipAttributeByKeyOptional(
        modelId: ModelId, relationshipId: RelationshipId, key: AttributeKey
    ): Attribute? {
        return findRelationshipAttributeByOptional(
            modelId, relationshipId, RelationshipAttributeTable.key eq key
        )
    }


    fun findRelationshipAttributeByOptional(
        modelId: ModelId, relationshipId: RelationshipId, criterion: Expression<Boolean>
    ): Attribute? {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val typeTable = ModelTypeTable.alias("relationship_attribute_type_snapshot")
        val attributeTagTable = RelationshipAttributeTagTable.alias("relationship_attribute_tag_snapshot")
        return RelationshipAttributeTable.join(
            RelationshipTable,
            JoinType.INNER,
            RelationshipAttributeTable.relationshipSnapshotId,
            RelationshipTable.id
        ).join(
            typeTable,
            JoinType.INNER,
            onColumn = RelationshipAttributeTable.typeSnapshotId,
            otherColumn = typeTable[ModelTypeTable.id]
        ).join(
            attributeTagTable,
            JoinType.LEFT,
            onColumn = RelationshipAttributeTable.id,
            otherColumn = attributeTagTable[RelationshipAttributeTagTable.attributeSnapshotId]
        ).selectAll()
            .where { (RelationshipTable.modelSnapshotId eq modelSnapshotId) and (RelationshipTable.lineageId eq relationshipId) and criterion }
            .orderBy(attributeTagTable[RelationshipAttributeTagTable.tagId] to SortOrder.ASC)
            .toList()
            .let { rows ->
                if (rows.isEmpty()) {
                    null
                } else {
                    val row = rows.first()
                    val record = RelationshipAttributeRecord.read(row)
                    val tags = rows
                        .mapNotNull { tagRow -> readOptionalTagId(tagRow, attributeTagTable[RelationshipAttributeTagTable.tagId]) }
                        .distinct()
                    toRelationshipAttribute(
                        record,
                        tags,
                        row[typeTable[ModelTypeTable.lineageId]],
                        row[RelationshipTable.lineageId]
                    )
                }
            }
    }

    fun isTypeUsedInEntityAttributes(
        modelId: ModelId, typeId: TypeId
    ): Boolean {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val typeSnapshotId = snapshots.currentHeadTypeSnapshotId(modelId, typeId)
        return EntityAttributeTable.join(
            EntityTable,
            JoinType.INNER,
            onColumn = EntityAttributeTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).selectAll().where {
            (EntityAttributeTable.typeSnapshotId eq typeSnapshotId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
        }.any()
    }

    fun isTypeUsedInRelationshipAttributes(
        modelId: ModelId, typeId: TypeId
    ): Boolean {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val typeSnapshotId = snapshots.currentHeadTypeSnapshotId(modelId, typeId)
        return RelationshipAttributeTable.join(
            RelationshipTable,
            JoinType.INNER,
            onColumn = RelationshipAttributeTable.relationshipSnapshotId,
            otherColumn = RelationshipTable.id
        ).selectAll().where {
            (RelationshipAttributeTable.typeSnapshotId eq typeSnapshotId) and (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }.any()
    }
}
