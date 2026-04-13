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

        snapWrite.modelInsert(
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
        cloneEntityPrimaryKeySnapshots(currentHeadSnapshotId, converters)
        cloneBusinessKeySnapshots(currentHeadSnapshotId, converters)
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
            snapWrite.modelAddTag(versionSnapshotId, tagId)
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
            snapWrite.typeInsert(
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
            snapWrite.entityInsert(
                EntityRecord(
                    snapshotId = converters.entity.convert(record.snapshotId),
                    lineageId = record.lineageId,
                    modelSnapshotId = versionSnapshotId,
                    key = record.key,
                    name = record.name,
                    description = record.description,
                    origin = record.origin,
                    documentationHome = record.documentationHome
                )
            )
        }

        for (record in currentHeadAttributeRecords) {
            snapWrite.entityAttributeInsert(
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
                snapWrite.entityAddTag(entry.value, tagId)
            }
        }
    }

    private fun cloneEntityAttributeTags(converters: Converters) {
        for (entry in converters.entityAttribute.map.entries) {
            val tagIds = EntityAttributeTagTable.select(EntityAttributeTagTable.tagId)
                .where { EntityAttributeTagTable.attributeSnapshotId eq entry.key }
                .map { it[EntityAttributeTagTable.tagId] }
            for (tagId in tagIds) {
                snapWrite.entityAttributeAddTag(entry.value, tagId)
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
            snapWrite.relationshipInsert(
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

    /**
     * Clones entity primary keys once entity and entity-attribute snapshots have been remapped.
     */
    private fun cloneEntityPrimaryKeySnapshots(
        currentHeadSnapshotId: ModelSnapshotId,
        converters: Converters
    ) {
        val rows = EntityPKTable.join(
            EntityTable,
            JoinType.INNER,
            onColumn = EntityPKTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).selectAll().where {
            EntityTable.modelSnapshotId eq currentHeadSnapshotId
        }
        for (row in rows) {
            val record = EntityPKRecord.read(row)
            val snapshotId = converters.entityPrimaryKey.generate(record.snapshotId)
            snapWrite.entityPrimaryKeyInsert(
                EntityPKRecord(
                    snapshotId = snapshotId,
                    lineageId = record.lineageId,
                    modelEntitySnapshotId = converters.entity.convert(record.modelEntitySnapshotId)
                )
            )
        }

        val attributeRows = EntityPKAttributeTable.selectAll().where {
            EntityPKAttributeTable.entityPKSnapshotId inList converters.entityPrimaryKey.map.keys.toList()
        }
        for (row in attributeRows) {
            snapWrite.entityPrimaryKeyAttributeInsert(
                entityPrimaryKeySnapshotId = converters.entityPrimaryKey.convert(row[EntityPKAttributeTable.entityPKSnapshotId]),
                attributeSnapshotId = converters.entityAttribute.convert(row[EntityPKAttributeTable.attributeSnapshotId]),
                priority = row[EntityPKAttributeTable.priority]
            )
        }
    }

    /**
     * Clones business keys once entity and entity-attribute snapshots have been remapped.
     */
    private fun cloneBusinessKeySnapshots(
        currentHeadSnapshotId: ModelSnapshotId,
        converters: Converters
    ) {
        val rows = BusinessKeyTable.join(
            EntityTable,
            JoinType.INNER,
            onColumn = BusinessKeyTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).selectAll().where {
            EntityTable.modelSnapshotId eq currentHeadSnapshotId
        }
        for (row in rows) {
            val record = BusinessKeyRecord.read(row)
            val versionBusinessKeySnapshotId = converters.businessKey.generate(record.snapshotId)
            snapWrite.businessKeyInsert(
                BusinessKeyRecord(
                    snapshotId = versionBusinessKeySnapshotId,
                    lineageId = record.lineageId,
                    modelEntitySnapshotId = converters.entity.convert(record.modelEntitySnapshotId),
                    key = record.key,
                    name = record.name,
                    description = record.description
                )
            )
        }

        val attributeRows = BusinessKeyAttributeTable.selectAll().where {
            BusinessKeyAttributeTable.businessKeySnapshotId inList converters.businessKey.map.keys.toList()
        }
        for (row in attributeRows) {
            snapWrite.businessKeyAttributeInsert(
                businessKeySnapshotId = converters.businessKey.convert(row[BusinessKeyAttributeTable.businessKeySnapshotId]),
                attributeSnapshotId = converters.entityAttribute.convert(row[BusinessKeyAttributeTable.attributeSnapshotId]),
                priority = row[BusinessKeyAttributeTable.priority]
            )
        }
    }

    private fun cloneRelationshipTags(converters: Converters) {
        for (entry in converters.relationship.map.entries) {
            val tagIds = RelationshipTagTable.select(RelationshipTagTable.tagId)
                .where { RelationshipTagTable.relationshipSnapshotId eq entry.key }
                .map { it[RelationshipTagTable.tagId] }
            for (tagId in tagIds) {
                snapWrite.relationshipAddTag(entry.value, tagId)
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
            snapWrite.relationshipRoleInsert(
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
            snapWrite.relationshipAttributeInsert(
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
                snapWrite.relationshipAttributeAddTag(entry.value, tagId)
            }
        }
    }

    private class Converters {
        val type = IdConv("type_snapshot_id", TypeSnapshotId::generate)
        val entity = IdConv("entity_snapshot_id", EntitySnapshotId::generate)
        val entityAttribute = IdConv("entity_attribute_snapshot_id", AttributeSnapshotId::generate)
        val entityPrimaryKey = IdConv("entity_primary_key_snapshot_id", EntityPKSnapshotId::generate)
        val businessKey = IdConv("business_key_snapshot_id", BusinessKeySnapshotId::generate)
        val relationship = IdConv("relationship_snapshot_id", RelationshipSnapshotId::generate)
        val relationshipAttribute = IdConv("relationship_attribute_snapshot_id", AttributeSnapshotId::generate)
    }
}
