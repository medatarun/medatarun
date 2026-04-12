package io.medatarun.model.infra.db.snapshots

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.ModelStorageDbMissingCurrentHeadModelSnapshotException
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.tables.*
import io.medatarun.model.ports.needs.ModelClock
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

internal class ModelStorageDbSnapshotCreate(
    private val clock: ModelClock,
    private val snapWrite: ModelStorageDbSnapshotWriter
) {

    /**
     * Creates a frozen VERSION_SNAPSHOT by cloning the current head rows and
     * remapping every internal snapshot reference to fresh snapshot ids.
     */
    fun createVersionSnapshotFromCurrentHead(
        modelId: ModelId,
        currentHeadSnapshotId: ModelSnapshotId,
        streamRevision: Int,
        modelEventId: ModelEventId,
        version: ModelVersion
    ) {
        val currentHeadRow =
            ModelSnapshotTable.selectAll().where { ModelSnapshotTable.id eq currentHeadSnapshotId }.singleOrNull()
                ?: throw ModelStorageDbMissingCurrentHeadModelSnapshotException(modelId)
        val currentHeadRecord = ModelRecord.read(currentHeadRow)
        val versionSnapshotId = ModelSnapshotId.generate()
        val now = clock.now()

        snapWrite.insertModel(
            ModelRecord(
                snapshotId = versionSnapshotId,
                modelId = currentHeadRecord.modelId,
                key = currentHeadRecord.key,
                name = currentHeadRecord.name,
                description = currentHeadRecord.description,
                version = version,
                origin = currentHeadRecord.origin,
                authority = currentHeadRecord.authority,
                documentationHome = currentHeadRecord.documentationHome,
                snapshotKind = ModelSnapshotKind.VERSION_SNAPSHOT,
                upToRevision = streamRevision,
                modelEventReleaseId = modelEventId,
                createdAt = now,
                updatedAt = now
            )
        )

        cloneModelTags(currentHeadSnapshotId, versionSnapshotId)

        val typeSnapshotIdMap = cloneTypeSnapshots(currentHeadSnapshotId, versionSnapshotId)
        val entitySnapshotIdMap = cloneEntitySnapshots(currentHeadSnapshotId, versionSnapshotId, typeSnapshotIdMap)
        val relationshipSnapshotIdMap = cloneRelationshipSnapshots(currentHeadSnapshotId, versionSnapshotId)

        cloneEntityTags(entitySnapshotIdMap)
        cloneRelationshipTags(relationshipSnapshotIdMap)
        cloneRelationshipRoleSnapshots(relationshipSnapshotIdMap, entitySnapshotIdMap)
        cloneRelationshipAttributeSnapshots(relationshipSnapshotIdMap, typeSnapshotIdMap)
    }

    private fun cloneModelTags(currentHeadSnapshotId: ModelSnapshotId, versionSnapshotId: ModelSnapshotId) {
        val tagIds = ModelTagTable.select(ModelTagTable.tagId)
            .where { ModelTagTable.modelSnapshotId eq currentHeadSnapshotId }
            .map { it[ModelTagTable.tagId] }
        for (tagId in tagIds) {
            snapWrite.insertModelTag(versionSnapshotId, tagId)
        }
    }

    private fun cloneTypeSnapshots(
        currentHeadSnapshotId: ModelSnapshotId,
        versionSnapshotId: ModelSnapshotId
    ): Map<TypeSnapshotId, TypeSnapshotId> {
        val rows = ModelTypeTable.selectAll().where { ModelTypeTable.modelSnapshotId eq currentHeadSnapshotId }
        val snapshotIdMap = mutableMapOf<TypeSnapshotId, TypeSnapshotId>()
        for (row in rows) {
            val record = ModelTypeRecord.read(row)
            val versionTypeSnapshotId = TypeSnapshotId.generate()
            snapshotIdMap[record.snapshotId] = versionTypeSnapshotId
            snapWrite.insertType(
                ModelTypeRecord(
                    snapshotId = versionTypeSnapshotId,
                    lineageId = record.lineageId,
                    modelSnapshotId = versionSnapshotId,
                    key = record.key,
                    name = record.name,
                    description = record.description
                )
            )
        }
        return snapshotIdMap
    }

    private fun cloneEntitySnapshots(
        currentHeadSnapshotId: ModelSnapshotId,
        versionSnapshotId: ModelSnapshotId,
        typeSnapshotIdMap: Map<TypeSnapshotId, TypeSnapshotId>
    ): Map<EntitySnapshotId, EntitySnapshotId> {
        val entityRows = EntityTable.selectAll().where { EntityTable.modelSnapshotId eq currentHeadSnapshotId }
        val currentHeadEntityRecords = entityRows.map { EntityRecord.read(it) }
        val currentHeadAttributeRecords = EntityAttributeTable.join(
            EntityTable,
            JoinType.INNER,
            onColumn = EntityAttributeTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).selectAll().where { EntityTable.modelSnapshotId eq currentHeadSnapshotId }
            .map { EntityAttributeRecord.read(it) }

        val entitySnapshotIdMap = mutableMapOf<EntitySnapshotId, EntitySnapshotId>()
        val attributeSnapshotIdMap = mutableMapOf<AttributeSnapshotId, AttributeSnapshotId>()

        for (record in currentHeadEntityRecords) {
            entitySnapshotIdMap[record.snapshotId] = EntitySnapshotId.generate()
        }
        for (record in currentHeadAttributeRecords) {
            attributeSnapshotIdMap[record.snapshotId] = AttributeSnapshotId.generate()
        }

        for (record in currentHeadEntityRecords) {
            snapWrite.insertEntity(
                EntityRecord(
                    snapshotId = entitySnapshotIdMap.getValue(record.snapshotId),
                    lineageId = record.lineageId,
                    modelSnapshotId = versionSnapshotId,
                    key = record.key,
                    name = record.name,
                    description = record.description,
                    identifierAttributeSnapshotId = attributeSnapshotIdMap.getValue(record.identifierAttributeSnapshotId),
                    origin = record.origin,
                    documentationHome = record.documentationHome
                )
            )
        }

        for (record in currentHeadAttributeRecords) {
            snapWrite.insertEntityAttribute(
                EntityAttributeRecord(
                    snapshotId = attributeSnapshotIdMap.getValue(record.snapshotId),
                    lineageId = record.lineageId,
                    entitySnapshotId = entitySnapshotIdMap.getValue(record.entitySnapshotId),
                    key = record.key,
                    name = record.name,
                    description = record.description,
                    typeSnapshotId = typeSnapshotIdMap.getValue(record.typeSnapshotId),
                    optional = record.optional
                )
            )
        }

        cloneEntityAttributeTags(attributeSnapshotIdMap)
        return entitySnapshotIdMap
    }

    private fun cloneEntityTags(entitySnapshotIdMap: Map<EntitySnapshotId, EntitySnapshotId>) {
        for (entry in entitySnapshotIdMap.entries) {
            val tagIds = EntityTagTable.select(EntityTagTable.tagId)
                .where { EntityTagTable.entitySnapshotId eq entry.key }
                .map { it[EntityTagTable.tagId] }
            for (tagId in tagIds) {
                snapWrite.insertEntityTag(entry.value, tagId)
            }
        }
    }

    private fun cloneEntityAttributeTags(attributeSnapshotIdMap: Map<AttributeSnapshotId, AttributeSnapshotId>) {
        for (entry in attributeSnapshotIdMap.entries) {
            val tagIds = EntityAttributeTagTable.select(EntityAttributeTagTable.tagId)
                .where { EntityAttributeTagTable.attributeSnapshotId eq entry.key }
                .map { it[EntityAttributeTagTable.tagId] }
            for (tagId in tagIds) {
                snapWrite.insertEntityAttributeTag(entry.value, tagId)
            }
        }
    }

    private fun cloneRelationshipSnapshots(
        currentHeadSnapshotId: ModelSnapshotId,
        versionSnapshotId: ModelSnapshotId
    ): Map<RelationshipSnapshotId, RelationshipSnapshotId> {
        val rows = RelationshipTable.selectAll().where { RelationshipTable.modelSnapshotId eq currentHeadSnapshotId }
        val snapshotIdMap = mutableMapOf<RelationshipSnapshotId, RelationshipSnapshotId>()
        for (row in rows) {
            val record = RelationshipRecord.read(row)
            val versionRelationshipSnapshotId = RelationshipSnapshotId.generate()
            snapshotIdMap[record.snapshotId] = versionRelationshipSnapshotId
            snapWrite.insertRelationship(
                RelationshipRecord(
                    snapshotId = versionRelationshipSnapshotId,
                    lineageId = record.lineageId,
                    modelSnapshotId = versionSnapshotId,
                    key = record.key,
                    name = record.name,
                    description = record.description
                ),
                emptyList()
            )
        }
        return snapshotIdMap
    }

    private fun cloneRelationshipTags(relationshipSnapshotIdMap: Map<RelationshipSnapshotId, RelationshipSnapshotId>) {
        for (entry in relationshipSnapshotIdMap.entries) {
            val tagIds = RelationshipTagTable.select(RelationshipTagTable.tagId)
                .where { RelationshipTagTable.relationshipSnapshotId eq entry.key }
                .map { it[RelationshipTagTable.tagId] }
            for (tagId in tagIds) {
                snapWrite.insertRelationshipTag(entry.value, tagId)
            }
        }
    }

    private fun cloneRelationshipRoleSnapshots(
        relationshipSnapshotIdMap: Map<RelationshipSnapshotId, RelationshipSnapshotId>,
        entitySnapshotIdMap: Map<EntitySnapshotId, EntitySnapshotId>
    ) {
        if (relationshipSnapshotIdMap.isEmpty()) {
            return
        }
        val rows = RelationshipRoleTable.selectAll().where {
            RelationshipRoleTable.relationshipSnapshotId inList relationshipSnapshotIdMap.keys.toList()
        }
        for (row in rows) {
            val record = RelationshipRoleRecord.read(row)
            snapWrite.insertRelationshipRole(
                RelationshipRoleRecord(
                    snapshotId = RelationshipRoleSnapshotId.generate(),
                    lineageId = record.lineageId,
                    relationshipSnapshotId = relationshipSnapshotIdMap.getValue(record.relationshipSnapshotId),
                    key = record.key,
                    entitySnapshotId = entitySnapshotIdMap.getValue(record.entitySnapshotId),
                    name = record.name,
                    cardinality = record.cardinality
                )
            )
        }
    }

    private fun cloneRelationshipAttributeSnapshots(
        relationshipSnapshotIdMap: Map<RelationshipSnapshotId, RelationshipSnapshotId>,
        typeSnapshotIdMap: Map<TypeSnapshotId, TypeSnapshotId>
    ) {
        if (relationshipSnapshotIdMap.isEmpty()) {
            return
        }
        val rows = RelationshipAttributeTable.selectAll().where {
            RelationshipAttributeTable.relationshipSnapshotId inList relationshipSnapshotIdMap.keys.toList()
        }
        val attributeSnapshotIdMap = mutableMapOf<AttributeSnapshotId, AttributeSnapshotId>()

        for (row in rows) {
            val record = RelationshipAttributeRecord.read(row)
            val versionAttributeSnapshotId = AttributeSnapshotId.generate()
            attributeSnapshotIdMap[record.snapshotId] = versionAttributeSnapshotId
            snapWrite.insertRelationshipAttribute(
                RelationshipAttributeRecord(
                    snapshotId = versionAttributeSnapshotId,
                    lineageId = record.lineageId,
                    relationshipSnapshotId = relationshipSnapshotIdMap.getValue(record.relationshipSnapshotId),
                    key = record.key,
                    name = record.name,
                    description = record.description,
                    typeSnapshotId = typeSnapshotIdMap.getValue(record.typeSnapshotId),
                    optional = record.optional
                )
            )
        }

        for (entry in attributeSnapshotIdMap.entries) {
            val tagIds = RelationshipAttributeTagTable.select(RelationshipAttributeTagTable.tagId)
                .where { RelationshipAttributeTagTable.attributeSnapshotId eq entry.key }
                .map { it[RelationshipAttributeTagTable.tagId] }
            for (tagId in tagIds) {
                snapWrite.insertRelationshipAttributeTag(entry.value, tagId)
            }
        }
    }
}
