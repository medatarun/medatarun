package io.medatarun.model.infra.db.snapshots

import io.medatarun.lang.idconv.IdConv
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
        val converters = Converters()
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

        cloneTypeSnapshots(currentHeadSnapshotId, versionSnapshotId, converters)
        cloneEntitySnapshots(currentHeadSnapshotId, versionSnapshotId, converters)
        cloneRelationshipSnapshots(currentHeadSnapshotId, versionSnapshotId, converters)

        cloneEntityTags(converters)
        cloneRelationshipTags(converters)
        cloneRelationshipRoleSnapshots(converters)
        cloneRelationshipAttributeSnapshots(converters)
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
        versionSnapshotId: ModelSnapshotId,
        converters: Converters
    ) {
        val rows = ModelTypeTable.selectAll().where { ModelTypeTable.modelSnapshotId eq currentHeadSnapshotId }
        for (row in rows) {
            val record = ModelTypeRecord.read(row)
            val versionTypeSnapshotId = converters.type.generate(record.snapshotId)
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
    }

    private fun cloneEntitySnapshots(
        currentHeadSnapshotId: ModelSnapshotId,
        versionSnapshotId: ModelSnapshotId,
        converters: Converters
    ) {
        val entityRows = EntityTable.selectAll().where { EntityTable.modelSnapshotId eq currentHeadSnapshotId }
        val currentHeadEntityRecords = entityRows.map { EntityRecord.read(it) }
        val currentHeadAttributeRecords = EntityAttributeTable.join(
            EntityTable,
            JoinType.INNER,
            onColumn = EntityAttributeTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).selectAll().where { EntityTable.modelSnapshotId eq currentHeadSnapshotId }
            .map { EntityAttributeRecord.read(it) }

        for (record in currentHeadEntityRecords) {
            converters.entity.generate(record.snapshotId)
        }
        for (record in currentHeadAttributeRecords) {
            converters.entityAttribute.generate(record.snapshotId)
        }

        for (record in currentHeadEntityRecords) {
            snapWrite.insertEntity(
                EntityRecord(
                    snapshotId = converters.entity.convert(record.snapshotId),
                    lineageId = record.lineageId,
                    modelSnapshotId = versionSnapshotId,
                    key = record.key,
                    name = record.name,
                    description = record.description,
                    identifierAttributeSnapshotId = converters.entityAttribute.convert(record.identifierAttributeSnapshotId),
                    origin = record.origin,
                    documentationHome = record.documentationHome
                )
            )
        }

        for (record in currentHeadAttributeRecords) {
            snapWrite.insertEntityAttribute(
                EntityAttributeRecord(
                    snapshotId = converters.entityAttribute.convert(record.snapshotId),
                    lineageId = record.lineageId,
                    entitySnapshotId = converters.entity.convert(record.entitySnapshotId),
                    key = record.key,
                    name = record.name,
                    description = record.description,
                    typeSnapshotId = converters.type.convert(record.typeSnapshotId),
                    optional = record.optional
                )
            )
        }

        cloneEntityAttributeTags(converters)
    }

    private fun cloneEntityTags(converters: Converters) {
        for (entry in converters.entity.map.entries) {
            val tagIds = EntityTagTable.select(EntityTagTable.tagId)
                .where { EntityTagTable.entitySnapshotId eq entry.key }
                .map { it[EntityTagTable.tagId] }
            for (tagId in tagIds) {
                snapWrite.insertEntityTag(entry.value, tagId)
            }
        }
    }

    private fun cloneEntityAttributeTags(converters: Converters) {
        for (entry in converters.entityAttribute.map.entries) {
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
        versionSnapshotId: ModelSnapshotId,
        converters: Converters
    ) {
        val rows = RelationshipTable.selectAll().where { RelationshipTable.modelSnapshotId eq currentHeadSnapshotId }
        for (row in rows) {
            val record = RelationshipRecord.read(row)
            val versionRelationshipSnapshotId = converters.relationship.generate(record.snapshotId)
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
    }

    private fun cloneRelationshipTags(converters: Converters) {
        for (entry in converters.relationship.map.entries) {
            val tagIds = RelationshipTagTable.select(RelationshipTagTable.tagId)
                .where { RelationshipTagTable.relationshipSnapshotId eq entry.key }
                .map { it[RelationshipTagTable.tagId] }
            for (tagId in tagIds) {
                snapWrite.insertRelationshipTag(entry.value, tagId)
            }
        }
    }

    private fun cloneRelationshipRoleSnapshots(converters: Converters) {
        if (converters.relationship.map.isEmpty()) {
            return
        }
        val rows = RelationshipRoleTable.selectAll().where {
            RelationshipRoleTable.relationshipSnapshotId inList converters.relationship.map.keys.toList()
        }
        for (row in rows) {
            val record = RelationshipRoleRecord.read(row)
            snapWrite.insertRelationshipRole(
                RelationshipRoleRecord(
                    snapshotId = RelationshipRoleSnapshotId.generate(),
                    lineageId = record.lineageId,
                    relationshipSnapshotId = converters.relationship.convert(record.relationshipSnapshotId),
                    key = record.key,
                    entitySnapshotId = converters.entity.convert(record.entitySnapshotId),
                    name = record.name,
                    cardinality = record.cardinality
                )
            )
        }
    }

    private fun cloneRelationshipAttributeSnapshots(converters: Converters) {
        if (converters.relationship.map.isEmpty()) {
            return
        }
        val rows = RelationshipAttributeTable.selectAll().where {
            RelationshipAttributeTable.relationshipSnapshotId inList converters.relationship.map.keys.toList()
        }

        for (row in rows) {
            val record = RelationshipAttributeRecord.read(row)
            val versionAttributeSnapshotId = converters.relationshipAttribute.generate(record.snapshotId)
            snapWrite.insertRelationshipAttribute(
                RelationshipAttributeRecord(
                    snapshotId = versionAttributeSnapshotId,
                    lineageId = record.lineageId,
                    relationshipSnapshotId = converters.relationship.convert(record.relationshipSnapshotId),
                    key = record.key,
                    name = record.name,
                    description = record.description,
                    typeSnapshotId = converters.type.convert(record.typeSnapshotId),
                    optional = record.optional
                )
            )
        }

        for (entry in converters.relationshipAttribute.map.entries) {
            val tagIds = RelationshipAttributeTagTable.select(RelationshipAttributeTagTable.tagId)
                .where { RelationshipAttributeTagTable.attributeSnapshotId eq entry.key }
                .map { it[RelationshipAttributeTagTable.tagId] }
            for (tagId in tagIds) {
                snapWrite.insertRelationshipAttributeTag(entry.value, tagId)
            }
        }
    }

    private class Converters {
        val type = IdConv("type_snapshot_id", TypeSnapshotId::generate)
        val entity = IdConv("entity_snapshot_id", EntitySnapshotId::generate)
        val entityAttribute = IdConv("entity_attribute_snapshot_id", AttributeSnapshotId::generate)
        val relationship = IdConv("relationship_snapshot_id", RelationshipSnapshotId::generate)
        val relationshipAttribute = IdConv("relationship_attribute_snapshot_id", AttributeSnapshotId::generate)
    }
}
