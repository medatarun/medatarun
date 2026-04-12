package io.medatarun.model.infra.db.snapshots

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.AttributeSnapshotId
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelSnapshotId
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.TypeId
import io.medatarun.model.infra.db.records.RelationshipAttributeRecord
import io.medatarun.model.infra.db.tables.RelationshipAttributeTable
import io.medatarun.model.infra.db.tables.RelationshipAttributeTagTable
import io.medatarun.model.infra.db.tables.RelationshipTable
import io.medatarun.model.ports.needs.ModelClock
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update

internal class ModelStorageDbSnapshotWriter(
    private val snapshots: ModelStorageDbSnapshots,
    private val clock: ModelClock,
) {


    // Relationship attribute
    // ------------------------------------------------------------------------

    fun updateRelationshipAttribute(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        block: (UpdateStatement) -> Unit
    ) {
        val relationshipIds = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }
        val attributeIds = RelationshipAttributeTable.select(RelationshipAttributeTable.id).where {
            (RelationshipAttributeTable.lineageId eq attributeId) and
                    (RelationshipAttributeTable.relationshipSnapshotId inSubQuery relationshipIds)
        }
        RelationshipAttributeTable.update(where = {
            RelationshipAttributeTable.id inSubQuery attributeIds
        }) { row ->
            block(row)
        }
    }

    fun updateRelationshipAttributeKey(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        key: AttributeKey
    ) {
        updateRelationshipAttribute(modelSnapshotId, relationshipId, attributeId) { row ->
            row[RelationshipAttributeTable.key] = key
        }
    }

    fun updateRelationshipAttributeName(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        name: LocalizedText?
    ) {
        updateRelationshipAttribute(modelSnapshotId, relationshipId, attributeId) { row ->
            row[RelationshipAttributeTable.name] = name
        }
    }

    fun updateRelationshipAttributeDescription(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        description: LocalizedMarkdown?
    ) {
        updateRelationshipAttribute(modelSnapshotId, relationshipId, attributeId) { row ->
            row[RelationshipAttributeTable.description] = description
        }
    }

    fun updateRelationshipAttributeType(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        typeId: TypeId
    ) {
        updateRelationshipAttribute(modelSnapshotId, relationshipId, attributeId) { row ->
            val typeSnapshotId = snapshots.currentHeadTypeSnapshotIdInModelSnapshot(modelSnapshotId, typeId)
            row[RelationshipAttributeTable.typeSnapshotId] = typeSnapshotId
        }
    }

    fun updateRelationshipAttributeOptional(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        optional: Boolean
    ) {
        updateRelationshipAttribute(modelSnapshotId, relationshipId, attributeId) { row ->
            row[RelationshipAttributeTable.optional] = optional
        }
    }

    fun addRelationshipAttributeTag(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        tagId: TagId
    ) {
        val relationshipIds = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }
        val attributeSnapshotId = RelationshipAttributeTable.select(RelationshipAttributeTable.id).where {
            (RelationshipAttributeTable.lineageId eq attributeId) and
                    (RelationshipAttributeTable.relationshipSnapshotId inSubQuery relationshipIds)
        }
            .single()[RelationshipAttributeTable.id]
        val exists = RelationshipAttributeTagTable.select(RelationshipAttributeTagTable.attributeSnapshotId).where {
            (RelationshipAttributeTagTable.attributeSnapshotId eq attributeSnapshotId) and (RelationshipAttributeTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertRelationshipAttributeTag(attributeSnapshotId, tagId)
        }

    }

    fun insertRelationshipAttributeTag(attributeId: AttributeSnapshotId, tagId: TagId) {
        RelationshipAttributeTagTable.insert { row ->
            row[RelationshipAttributeTagTable.attributeSnapshotId] = attributeId
            row[RelationshipAttributeTagTable.tagId] = tagId
        }
    }

    fun insertRelationshipAttribute(
        record: RelationshipAttributeRecord
    ) {
        RelationshipAttributeTable.insert { row ->
            row[RelationshipAttributeTable.id] = record.snapshotId
            row[RelationshipAttributeTable.lineageId] = record.lineageId
            row[RelationshipAttributeTable.relationshipSnapshotId] = record.relationshipSnapshotId
            row[RelationshipAttributeTable.key] = record.key
            row[RelationshipAttributeTable.name] = record.name
            row[RelationshipAttributeTable.description] = record.description
            row[RelationshipAttributeTable.typeSnapshotId] = record.typeSnapshotId
            row[RelationshipAttributeTable.optional] = record.optional
        }

    }

    fun deleteRelationshipAttribute(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId
    ) {
        val relationshipIds = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }
        val attributeIds = RelationshipAttributeTable.select(RelationshipAttributeTable.id).where {
            (RelationshipAttributeTable.lineageId eq attributeId) and
                    (RelationshipAttributeTable.relationshipSnapshotId inSubQuery relationshipIds)
        }
        RelationshipAttributeTable.deleteWhere {
            RelationshipAttributeTable.id inSubQuery attributeIds
        }
    }

    fun deleteRelationshipAttributeTag(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        tagId: TagId
    ) {
        val relationshipIds = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }
        val attributeIds = RelationshipAttributeTable.select(RelationshipAttributeTable.id).where {
            (RelationshipAttributeTable.lineageId eq attributeId) and
                    (RelationshipAttributeTable.relationshipSnapshotId inSubQuery relationshipIds)
        }
        RelationshipAttributeTagTable.deleteWhere {
            (RelationshipAttributeTagTable.attributeSnapshotId inSubQuery attributeIds) and
                    (RelationshipAttributeTagTable.tagId eq tagId)
        }

    }
}