package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.ModelStorageAdapters.toEntity
import io.medatarun.model.infra.db.ModelStorageAdapters.toEntityAttribute
import io.medatarun.model.infra.db.ModelStorageAdapters.toModel
import io.medatarun.model.infra.db.ModelStorageAdapters.toModelChangeEvent
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationship
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationshipAttribute
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationshipRole
import io.medatarun.model.infra.db.ModelStorageAdapters.toType
import io.medatarun.model.infra.db.aggregate.ModelStorageDbAggregateReader
import io.medatarun.model.infra.db.events.ModelEventRegistry
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.snapshots.SnapshotSelector
import io.medatarun.model.infra.db.tables.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

class ModelStorageDbRead(
    private val modelEventRegistry: ModelEventRegistry,
    private val aggregateReader: ModelStorageDbAggregateReader
) {



    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    // Model

    fun existsModelById(id: ModelId): Boolean {
        return ModelTable.select(ModelTable.id).where { ModelTable.id eq id }.limit(1).any()
    }

    fun existsModelByKey(key: ModelKey): Boolean {
        return ModelSnapshotTable.select(ModelSnapshotTable.id).where {
            (ModelSnapshotTable.snapshotKind eq ModelSnapshotKind.CURRENT_HEAD) and (ModelSnapshotTable.key eq key)
        }.limit(1).any()
    }

    fun findAllModelIds(): List<ModelId> {
        return ModelTable.selectAll().map { it[ModelTable.id] }
    }

    fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate? {
        return aggregateReader.loadModelAggregateOptional(
            SnapshotSelector.CurrentHeadByKey(key)
        )
    }

    fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate? {
        return aggregateReader.loadModelAggregateOptional(
            SnapshotSelector.CurrentHeadByModelId(id)
        )
    }


    fun findModelAggregateVersionOptional(modelId: ModelId, modelVersion: ModelVersion): ModelAggregate? {
        return aggregateReader.loadModelAggregateOptional(
            SnapshotSelector.ByVersion(modelId, modelVersion)
        )
    }

    fun findAllModelEvents(modelId: ModelId): List<ModelEventRecord> {
        return ModelEventTable.selectAll()
            .where { ModelEventTable.modelId eq modelId }
            .orderBy(ModelEventTable.streamRevision to SortOrder.ASC)
            .map(ModelEventRecord::read)
    }

    fun findModelByKeyOptional(key: ModelKey): Model? {
        val row = ModelSnapshotTable.selectAll().where {
            (ModelSnapshotTable.snapshotKind eq ModelSnapshotKind.CURRENT_HEAD) and (ModelSnapshotTable.key eq key)
        }.singleOrNull()
        return if (row == null) null else toModel(ModelRecord.read(row))
    }

    fun findModelByIdOptional(id: ModelId): Model? {
        val row = ModelSnapshotTable.selectAll().where {
            (ModelSnapshotTable.snapshotKind eq ModelSnapshotKind.CURRENT_HEAD) and (ModelSnapshotTable.modelId eq id)
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
                row[ModelEventTable.modelVersion]
                    ?: throw ModelStorageDbInvalidReleaseEventException(modelId, row[ModelEventTable.id])

            }
    }

    fun findTypeByKeyOptional(
        modelId: ModelId, key: TypeKey
    ): ModelType? {
        return findTypeByOptional(modelId, ModelTypeTable.key eq key)
    }

    fun findTypeByIdOptional(
        modelId: ModelId, typeId: TypeId
    ): ModelType? {
        return findTypeByOptional(modelId, ModelTypeTable.lineageId eq typeId)
    }

    private fun findTypeByOptional(
        modelId: ModelId,
        criterion: Op<Boolean>
    ): ModelType? {
        return ModelTypeTable.join(
            ModelSnapshotTable,
            JoinType.INNER,
            onColumn = ModelTypeTable.modelSnapshotId,
            otherColumn = ModelSnapshotTable.id
        ).selectAll().where {
            SnapshotSelector.CurrentHeadByModelId(modelId).criterion() and criterion
        }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
    }

    fun findEntityByIdOptional(
        modelId: ModelId, entityId: EntityId
    ): Entity? {
        return findEntityByOptional(modelId, EntityTable.lineageId eq entityId)
    }

    fun findEntityByKeyOptional(
        modelId: ModelId, entityKey: EntityKey
    ): Entity? {
        return findEntityByOptional(modelId, EntityTable.key eq entityKey)
    }

    private fun findEntityByOptional(
        modelId: ModelId,
        criterion: Op<Boolean>
    ): Entity? {
        val identifierAttributeTable = EntityAttributeTable.alias("identifier_attribute_snapshot")
        val entityTagTable = EntityTagTable.alias("entity_tag_snapshot")
        return EntityTable.join(
            ModelSnapshotTable,
            JoinType.INNER,
            onColumn = EntityTable.modelSnapshotId,
            otherColumn = ModelSnapshotTable.id
        ).join(
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
            SnapshotSelector.CurrentHeadByModelId(modelId).criterion() and criterion
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
        return findEntityAttributeByOptional(
            modelId,
            entityId,
            EntityAttributeTable.lineageId eq attributeId
        )
    }

    fun findEntityAttributeByKeyOptional(
        modelId: ModelId, entityId: EntityId, key: AttributeKey
    ): Attribute? {
        return findEntityAttributeByOptional(
            modelId,
            entityId,
            EntityAttributeTable.key eq key
        )
    }

    private fun findEntityAttributeByOptional(
        modelId: ModelId,
        entityId: EntityId,
        criterion: Op<Boolean>
    ): Attribute? {
        val typeTable = ModelTypeTable.alias("entity_attribute_type_snapshot")
        val attributeTagTable = EntityAttributeTagTable.alias("entity_attribute_tag_snapshot")
        return EntityAttributeTable.join(
            EntityTable, JoinType.INNER, EntityAttributeTable.entitySnapshotId, EntityTable.id
        ).join(
            ModelSnapshotTable,
            JoinType.INNER,
            onColumn = EntityTable.modelSnapshotId,
            otherColumn = ModelSnapshotTable.id
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
            .where { SnapshotSelector.CurrentHeadByModelId(modelId).criterion() and (EntityTable.lineageId eq entityId) and criterion }
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
        val roleEntityTable = EntityTable.alias("relationship_role_entity_snapshot")
        val relationshipRows = RelationshipTable.join(
            ModelSnapshotTable,
            JoinType.INNER,
            onColumn = RelationshipTable.modelSnapshotId,
            otherColumn = ModelSnapshotTable.id
        ).join(
            RelationshipRoleTable,
            JoinType.INNER,
            onColumn = RelationshipTable.id,
            otherColumn = RelationshipRoleTable.relationshipSnapshotId
        ).join(
            roleEntityTable,
            JoinType.INNER,
            onColumn = RelationshipRoleTable.entitySnapshotId,
            otherColumn = roleEntityTable[EntityTable.id]
        ).selectAll().where { SnapshotSelector.CurrentHeadByModelId(modelId).criterion() and criterion }
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
        // Roles and tags are two independent collections on the same relationship.
        // Joining both in one query multiplies rows (roles x tags), which makes
        // reconstruction and deduplication more fragile than a second query.
        // We had bugs about that before, so the choice is 2 requests.
        val tags = RelationshipTagTable.selectAll()
            .where { RelationshipTagTable.relationshipSnapshotId eq relationshipRecord.snapshotId }
            .orderBy(RelationshipTagTable.tagId to SortOrder.ASC)
            .map { it[RelationshipTagTable.tagId] }

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
        val roleEntityTable = EntityTable.alias("relationship_role_entity_snapshot")
        return RelationshipRoleTable.join(
            RelationshipTable,
            JoinType.INNER,
            onColumn = RelationshipRoleTable.relationshipSnapshotId,
            otherColumn = RelationshipTable.id
        ).join(
            ModelSnapshotTable,
            JoinType.INNER,
            onColumn = RelationshipTable.modelSnapshotId,
            otherColumn = ModelSnapshotTable.id
        ).join(
            roleEntityTable,
            JoinType.INNER,
            onColumn = RelationshipRoleTable.entitySnapshotId,
            otherColumn = roleEntityTable[EntityTable.id]
        ).selectAll().where {
            SnapshotSelector.CurrentHeadByModelId(modelId).criterion() and (RelationshipTable.lineageId eq relationshipId) and criterion
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
        val typeTable = ModelTypeTable.alias("relationship_attribute_type_snapshot")
        val attributeTagTable = RelationshipAttributeTagTable.alias("relationship_attribute_tag_snapshot")
        return RelationshipAttributeTable.join(
            RelationshipTable,
            JoinType.INNER,
            RelationshipAttributeTable.relationshipSnapshotId,
            RelationshipTable.id
        ).join(
            ModelSnapshotTable,
            JoinType.INNER,
            onColumn = RelationshipTable.modelSnapshotId,
            otherColumn = ModelSnapshotTable.id
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
            .where { SnapshotSelector.CurrentHeadByModelId(modelId).criterion() and (RelationshipTable.lineageId eq relationshipId) and criterion }
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
        return EntityAttributeTable.join(
            EntityTable,
            JoinType.INNER,
            onColumn = EntityAttributeTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).join(
            ModelTypeTable,
            JoinType.INNER,
            onColumn = EntityAttributeTable.typeSnapshotId,
            otherColumn = ModelTypeTable.id
        ).join(
            ModelSnapshotTable,
            JoinType.INNER,
            onColumn = EntityTable.modelSnapshotId,
            otherColumn = ModelSnapshotTable.id
        ).selectAll().where {
            SnapshotSelector.CurrentHeadByModelId(modelId).criterion() and
                    (ModelTypeTable.lineageId eq typeId) and
                (ModelTypeTable.modelSnapshotId eq ModelSnapshotTable.id)
        }.any()
    }

    fun isTypeUsedInRelationshipAttributes(
        modelId: ModelId, typeId: TypeId
    ): Boolean {
        return RelationshipAttributeTable.join(
            RelationshipTable,
            JoinType.INNER,
            onColumn = RelationshipAttributeTable.relationshipSnapshotId,
            otherColumn = RelationshipTable.id
        ).join(
            ModelTypeTable,
            JoinType.INNER,
            onColumn = RelationshipAttributeTable.typeSnapshotId,
            otherColumn = ModelTypeTable.id
        ).join(
            ModelSnapshotTable,
            JoinType.INNER,
            onColumn = RelationshipTable.modelSnapshotId,
            otherColumn = ModelSnapshotTable.id
        ).selectAll().where {
            SnapshotSelector.CurrentHeadByModelId(modelId).criterion() and
                    (ModelTypeTable.lineageId eq typeId) and
                (ModelTypeTable.modelSnapshotId eq ModelSnapshotTable.id)
        }.any()
    }

    fun findModelVersions(modelId: ModelId): List<ModelChangeEvent> {
        return ModelEventTable.selectAll()
            .where {
                (ModelEventTable.modelId eq modelId) and
                        (ModelEventTable.eventType eq modelEventRegistry.modelReleaseEventType())
                        (ModelEventTable.modelVersion neq null)
            }
            .orderBy(ModelEventTable.streamRevision to SortOrder.ASC)
            .map(ModelEventRecord::read)
            .map { record -> toModelChangeEvent(record) }
    }

    fun findModelChangeEventsInVersion(modelId: ModelId, version: ModelVersion): List<ModelChangeEvent> {
        val releaseRevision = ModelEventTable
            .select(ModelEventTable.streamRevision)
            .where {
                (ModelEventTable.modelId eq modelId) and
                    (ModelEventTable.eventType eq modelEventRegistry.modelReleaseEventType()) and
                    (ModelEventTable.modelVersion eq version)
            }
            .orderBy(ModelEventTable.streamRevision to SortOrder.ASC)
            .limit(1)
            .singleOrNull()
            ?.let { row -> row[ModelEventTable.streamRevision] }
            ?: throw ModelStorageDbMissingReleaseEventException(modelId, version.asString())

        val previousReleaseRevision = ModelEventTable
            .select(ModelEventTable.streamRevision)
            .where {
                (ModelEventTable.modelId eq modelId) and
                    (ModelEventTable.eventType eq modelEventRegistry.modelReleaseEventType()) and
                    (ModelEventTable.streamRevision less releaseRevision)
            }
            .orderBy(ModelEventTable.streamRevision to SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.let { row -> row[ModelEventTable.streamRevision] }

        return ModelEventTable.selectAll()
            .where {
                (ModelEventTable.modelId eq modelId) and
                    (ModelEventTable.streamRevision lessEq releaseRevision) and
                    if (previousReleaseRevision == null) {
                        Op.TRUE
                    } else {
                        ModelEventTable.streamRevision greater previousReleaseRevision
                    }
            }
            .orderBy(ModelEventTable.streamRevision to SortOrder.ASC)
            .map(ModelEventRecord::read)
            .map { record -> toModelChangeEvent(record) }
    }

    fun findModelChangeEventsSinceLastReleaseEvent(modelId: ModelId): List<ModelChangeEvent> {
        val releaseRevision = ModelEventTable
            .select(ModelEventTable.streamRevision)
            .where {
                (ModelEventTable.modelId eq modelId) and
                    (ModelEventTable.eventType eq modelEventRegistry.modelReleaseEventType())
            }
            .orderBy(ModelEventTable.streamRevision to SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.let { row -> row[ModelEventTable.streamRevision] }
            ?: throw ModelStorageDbNoReleaseException(modelId)

        return ModelEventTable.selectAll()
            .where {
                (ModelEventTable.modelId eq modelId) and
                    (ModelEventTable.streamRevision greater releaseRevision)
            }
            .orderBy(ModelEventTable.streamRevision to SortOrder.ASC)
            .map(ModelEventRecord::read)
            .map { record -> toModelChangeEvent(record) }
    }

}
