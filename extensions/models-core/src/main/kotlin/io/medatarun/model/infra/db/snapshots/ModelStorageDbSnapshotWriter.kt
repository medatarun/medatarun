package io.medatarun.model.infra.db.snapshots

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.tables.*
import io.medatarun.model.ports.needs.ModelClock
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.jdbc.*

internal class ModelStorageDbSnapshotWriter(
    private val snapshots: ModelStorageDbSnapshots,
    private val clock: ModelClock,
) {

    // Model
    // ------------------------------------------------------------------------

    fun modelInsert(record: ModelRecord) {
        ModelSnapshotTable.insert { row ->
            row[ModelSnapshotTable.id] = record.snapshotId
            row[ModelSnapshotTable.modelId] = record.modelId
            row[ModelSnapshotTable.key] = record.key
            row[ModelSnapshotTable.name] = record.name
            row[ModelSnapshotTable.description] = record.description
            row[ModelSnapshotTable.origin] = record.origin
            row[ModelSnapshotTable.authority] = record.authority
            row[ModelSnapshotTable.documentationHome] = record.documentationHome
            row[ModelSnapshotTable.snapshotKind] = record.snapshotKind
            row[ModelSnapshotTable.upToRevision] = record.upToRevision
            row[ModelSnapshotTable.modelEventReleaseId] = record.modelEventReleaseId
            row[ModelSnapshotTable.version] = record.version
            row[ModelSnapshotTable.createdAt] = record.createdAt
            row[ModelSnapshotTable.updatedAt] = record.updatedAt
        }
    }

    fun modelUpdateName(modelSnapshotId: ModelSnapshotId, name: LocalizedText?) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq modelSnapshotId }) { row ->
            row[ModelSnapshotTable.name] = name
        }
    }

    fun modelUpdateKey(modelSnapshotId: ModelSnapshotId, key: ModelKey) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq modelSnapshotId }) { row ->
            row[ModelSnapshotTable.key] = key
        }
    }

    fun modelUpdateDescription(modelSnapshotId: ModelSnapshotId, description: LocalizedMarkdown?) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq modelSnapshotId }) { row ->
            row[ModelSnapshotTable.description] = description
        }
    }

    fun modelUpdateAuthority(modelSnapshotId: ModelSnapshotId, authority: ModelAuthority) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq modelSnapshotId }) { row ->
            row[ModelSnapshotTable.authority] = authority
        }
    }

    fun modelUpdateDocumentationHome(modelSnapshotId: ModelSnapshotId, documentationHome: String?) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq modelSnapshotId }) { row ->
            row[ModelSnapshotTable.documentationHome] = documentationHome
        }
    }

    fun modelUpdateVersion(modelSnapshotId: ModelSnapshotId, version: ModelVersion) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq modelSnapshotId }) { row ->
            row[ModelSnapshotTable.version] = version
        }
    }

    fun modelAddTagIfNotExists(modelSnapshotId: ModelSnapshotId, tagId: TagId) {
        val exists = ModelTagTable.select(ModelTagTable.modelSnapshotId).where {
            (ModelTagTable.modelSnapshotId eq modelSnapshotId) and (ModelTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            modelAddTag(modelSnapshotId, tagId)
        }
    }

    fun modelAddTag(modelSnapshotId: ModelSnapshotId, tagId: TagId) {
        ModelTagTable.insert { row ->
            row[ModelTagTable.modelSnapshotId] = modelSnapshotId
            row[ModelTagTable.tagId] = tagId
        }
    }

    fun modelDeleteTag(modelSnapshotId: ModelSnapshotId, tagId: TagId) {
        ModelTagTable.deleteWhere {
            (ModelTagTable.modelSnapshotId eq modelSnapshotId) and (ModelTagTable.tagId eq tagId)
        }
    }

    // Types
    // ------------------------------------------------------------------------

    fun typeInsert(record: ModelTypeRecord) {
        ModelTypeTable.insert { row ->
            row[ModelTypeTable.id] = record.snapshotId
            row[ModelTypeTable.lineageId] = record.lineageId
            row[ModelTypeTable.modelSnapshotId] = record.modelSnapshotId
            row[ModelTypeTable.key] = record.key
            row[ModelTypeTable.name] = record.name
            row[ModelTypeTable.description] = record.description
        }
    }

    fun typeUpdateKey(modelSnapshotId: ModelSnapshotId, typeId: TypeId, key: TypeKey) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[ModelTypeTable.key] = key
        }
    }

    fun typeUpdateName(modelSnapshotId: ModelSnapshotId, typeId: TypeId, name: LocalizedText?) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[ModelTypeTable.name] = name
        }
    }

    fun typeUpdateDescription(modelSnapshotId: ModelSnapshotId, typeId: TypeId, description: LocalizedMarkdown?) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[ModelTypeTable.description] = description
        }
    }

    fun typeDelete(modelSnapshotId: ModelSnapshotId, typeId: TypeId) {
        ModelTypeTable.deleteWhere {
            (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
        }
    }

    // Entity
    // ------------------------------------------------------------------------

    fun entityInsert(record: EntityRecord) {
        EntityTable.insert { row ->
            row[EntityTable.id] = record.snapshotId
            row[EntityTable.lineageId] = record.lineageId
            row[EntityTable.modelSnapshotId] = record.modelSnapshotId
            row[EntityTable.key] = record.key
            row[EntityTable.name] = record.name
            row[EntityTable.description] = record.description
            row[EntityTable.origin] = record.origin
            row[EntityTable.documentationHome] = record.documentationHome
        }
    }

    fun entityUpdateKey(modelSnapshotId: ModelSnapshotId, entityId: EntityId, key: EntityKey) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[EntityTable.key] = key
        }
    }

    fun entityUpdateName(modelSnapshotId: ModelSnapshotId, entityId: EntityId, name: LocalizedText?) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[EntityTable.name] = name
        }
    }

    fun entityUpdateDescription(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        description: LocalizedMarkdown?
    ) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[EntityTable.description] = description
        }
    }


    fun entityUpdateDocumentationHome(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        documentationHome: String?
    ) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[EntityTable.documentationHome] = documentationHome
        }
    }

    fun entityAddTagIfNotExist(modelSnapshotId: ModelSnapshotId, entityId: EntityId, tagId: TagId) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(modelSnapshotId, entityId)
        val exists = EntityTagTable.select(EntityTagTable.entitySnapshotId).where {
            (EntityTagTable.entitySnapshotId eq entitySnapshotId) and (EntityTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            entityAddTag(entitySnapshotId, tagId)
        }
    }

    fun entityAddTag(entityId: EntitySnapshotId, tagId: TagId) {
        EntityTagTable.insert { row ->
            row[EntityTagTable.entitySnapshotId] = entityId
            row[EntityTagTable.tagId] = tagId
        }
    }

    fun entityDeleteTag(modelSnapshotId: ModelSnapshotId, entityId: EntityId, tagId: TagId) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(modelSnapshotId, entityId)
        EntityTagTable.deleteWhere {
            (EntityTagTable.entitySnapshotId eq entitySnapshotId) and (EntityTagTable.tagId eq tagId)
        }
    }

    fun entityPrimaryKeyInsert(
        entityPKSnapshotId: EntityPKSnapshotId,
        entitySnapshotId: EntitySnapshotId,
        attributeSnapshotIds: List<AttributeSnapshotId>
    ) {
        entityPrimaryKeyInsert(
            EntityPKRecord(
                snapshotId = entityPKSnapshotId,
                lineageId = EntityPrimaryKeyId.generate(),
                modelEntitySnapshotId = entitySnapshotId
            )
        )
        entityPrimaryKeyAttributesInsert(entityPKSnapshotId, attributeSnapshotIds)
    }

    fun entityPrimaryKeyInsert(record: EntityPKRecord) {
        EntityPKTable.insert { row ->
            row[EntityPKTable.id] = record.snapshotId
            row[EntityPKTable.lineageId] = record.lineageId
            row[EntityPKTable.entitySnapshotId] = record.modelEntitySnapshotId
        }
    }

    private fun entityPrimaryKeyAttributesInsert(
        entityPKSnapshotId: EntityPKSnapshotId,
        attributeSnapshotIds: List<AttributeSnapshotId>
    ) {
        var currentPosition = DEFAULT_ENTITY_PRIMARY_KEY_PRIORITY
        for (attributeSnapshotId in attributeSnapshotIds) {
            entityPrimaryKeyAttributeInsert(
                entityPrimaryKeySnapshotId = entityPKSnapshotId,
                attributeSnapshotId = attributeSnapshotId,
                priority = currentPosition++
            )
        }
    }

    fun entityPrimaryKeyAttributeInsert(
        entityPrimaryKeySnapshotId: EntityPKSnapshotId,
        attributeSnapshotId: AttributeSnapshotId,
        priority: Int
    ) {
        EntityPKAttributeTable.insert { row ->
            row[EntityPKAttributeTable.entityPKSnapshotId] = entityPrimaryKeySnapshotId
            row[EntityPKAttributeTable.priority] = priority
            row[EntityPKAttributeTable.attributeSnapshotId] = attributeSnapshotId
        }
    }
    /**
     *
     */
    fun entityPrimaryKeyUpdate(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeIds: List<AttributeId>
    ) {


        val entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(modelSnapshotId, entityId)
        val entityPKSnapshotIds = EntityPKTable.select(EntityPKTable.id)
            .where { EntityPKTable.entitySnapshotId eq entitySnapshotId }
            .orderBy(EntityPKTable.id to SortOrder.ASC)
            .map { it[EntityPKTable.id] }

        fun toAttributeSnapshotIds(): List<AttributeSnapshotId> {
            return attributeIds.map { attributeId ->
                EntityAttributeTable.selectAll()
                    .where { (EntityAttributeTable.entitySnapshotId eq entitySnapshotId) and (EntityAttributeTable.lineageId eq attributeId) }
                    .single()[EntityAttributeTable.id]
            }
        }

        if (entityPKSnapshotIds.isEmpty() && attributeIds.isNotEmpty()) {
            // no pk exist, and we get some attributes -> create
            entityPrimaryKeyInsert(
                entityPKSnapshotId = EntityPKSnapshotId.generate(),
                entitySnapshotId = entitySnapshotId,
                attributeSnapshotIds = toAttributeSnapshotIds()
            )
        } else if (attributeIds.isEmpty()) {
            // attributeIds is empty (it's a deletion request), and some pk exist -> delete all
            for (pkId in entityPKSnapshotIds) {
                EntityPKTable.deleteWhere { EntityPKTable.id eq pkId }
            }
        } else {
            // update: remove all attributes from found PK and recreate them in order
            // don't try to fix each row here because mostly there are 1 or 2 records max
            val attributeSnapshotIds = toAttributeSnapshotIds()
            for (id in entityPKSnapshotIds) {
                EntityPKAttributeTable.deleteWhere { EntityPKAttributeTable.entityPKSnapshotId eq id }
                entityPrimaryKeyAttributesInsert(id, attributeSnapshotIds)
            }
        }
    }


    fun entityDelete(modelSnapshotId: ModelSnapshotId, entityId: EntityId) {
        EntityTable.deleteWhere {
            (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
        }
    }

    // Entity attribute
    // ------------------------------------------------------------------------

    fun entityAttributeInsert(record: EntityAttributeRecord) {
        EntityAttributeTable.insert { row ->
            row[EntityAttributeTable.id] = record.snapshotId
            row[EntityAttributeTable.lineageId] = record.lineageId
            row[EntityAttributeTable.entitySnapshotId] = record.entitySnapshotId
            row[EntityAttributeTable.key] = record.key
            row[EntityAttributeTable.name] = record.name
            row[EntityAttributeTable.description] = record.description
            row[EntityAttributeTable.typeSnapshotId] = record.typeSnapshotId
            row[EntityAttributeTable.optional] = record.optional
        }
    }

    private fun entityAttributeUpdate(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        block: (UpdateStatement) -> Unit
    ) {
        val entityIds = EntityTable.select(EntityTable.id).where {
            (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
        }
        val attributeIds = EntityAttributeTable.select(EntityAttributeTable.id).where {
            (EntityAttributeTable.lineageId eq attributeId) and
                    (EntityAttributeTable.entitySnapshotId inSubQuery entityIds)
        }
        EntityAttributeTable.update(where = {
            EntityAttributeTable.id inSubQuery attributeIds
        }) { row ->
            block(row)
        }
    }

    fun entityAttributeUpdateKey(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        key: AttributeKey
    ) {
        entityAttributeUpdate(modelSnapshotId, entityId, attributeId) { row ->
            row[EntityAttributeTable.key] = key
        }
    }

    fun entityAttributeUpdateName(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        name: LocalizedText?
    ) {
        entityAttributeUpdate(modelSnapshotId, entityId, attributeId) { row ->
            row[EntityAttributeTable.name] = name
        }
    }

    fun entityAttributeUpdateDescription(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        description: LocalizedMarkdown?
    ) {
        entityAttributeUpdate(modelSnapshotId, entityId, attributeId) { row ->
            row[EntityAttributeTable.description] = description
        }
    }

    fun entityAttributeUpdateType(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        typeId: TypeId
    ) {
        entityAttributeUpdate(modelSnapshotId, entityId, attributeId) { row ->
            row[EntityAttributeTable.typeSnapshotId] =
                snapshots.currentHeadTypeSnapshotIdInModelSnapshot(modelSnapshotId, typeId)
        }
    }

    fun entityAttributeUpdateOptional(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        optional: Boolean
    ) {
        entityAttributeUpdate(modelSnapshotId, entityId, attributeId) { row ->
            row[EntityAttributeTable.optional] = optional
        }
    }

    fun entityAttributeAddTagIfNotExists(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        tagId: TagId
    ) {
        val entityIds = EntityTable.select(EntityTable.id).where {
            (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
        }
        val attributeSnapshotId = EntityAttributeTable.select(EntityAttributeTable.id).where {
            (EntityAttributeTable.lineageId eq attributeId) and
                    (EntityAttributeTable.entitySnapshotId inSubQuery entityIds)
        }
            .single()[EntityAttributeTable.id]
        val exists = EntityAttributeTagTable.select(EntityAttributeTagTable.attributeSnapshotId).where {
            (EntityAttributeTagTable.attributeSnapshotId eq attributeSnapshotId) and (EntityAttributeTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            entityAttributeAddTag(attributeSnapshotId, tagId)
        }
    }

    fun entityAttributeAddTag(attributeId: AttributeSnapshotId, tagId: TagId) {
        EntityAttributeTagTable.insert { row ->
            row[EntityAttributeTagTable.attributeSnapshotId] = attributeId
            row[EntityAttributeTagTable.tagId] = tagId
        }
    }

    fun entityAttributeDeleteTag(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        tagId: TagId
    ) {
        val entityIds = EntityTable.select(EntityTable.id).where {
            (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
        }
        val attributeIds = EntityAttributeTable.select(EntityAttributeTable.id).where {
            (EntityAttributeTable.lineageId eq attributeId) and
                    (EntityAttributeTable.entitySnapshotId inSubQuery entityIds)
        }
        EntityAttributeTagTable.deleteWhere {
            (EntityAttributeTagTable.attributeSnapshotId inSubQuery attributeIds) and
                    (EntityAttributeTagTable.tagId eq tagId)
        }
    }

    fun entityAttributeDelete(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId
    ) {
        val entityIds = EntityTable.select(EntityTable.id).where {
            (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
        }
        val attributeIds = EntityAttributeTable.select(EntityAttributeTable.id).where {
            (EntityAttributeTable.lineageId eq attributeId) and
                    (EntityAttributeTable.entitySnapshotId inSubQuery entityIds)
        }
        EntityAttributeTable.deleteWhere {
            EntityAttributeTable.id inSubQuery attributeIds
        }
    }

    // Relationship
    // ------------------------------------------------------------------------

    fun relationshipInsert(record: RelationshipRecord, roles: List<RelationshipRoleRecord>) {
        RelationshipTable.insert { row ->
            row[RelationshipTable.id] = record.snapshotId
            row[RelationshipTable.lineageId] = record.lineageId
            row[RelationshipTable.modelSnapshotId] = record.modelSnapshotId
            row[RelationshipTable.key] = record.key
            row[RelationshipTable.name] = record.name
            row[RelationshipTable.description] = record.description
        }
        for (roleRecord in roles) {
            relationshipRoleInsert(roleRecord)
        }
    }


    fun relationshipUpdateKey(modelSnapshotId: ModelSnapshotId, relationshipId: RelationshipId, key: RelationshipKey) {
        RelationshipTable.update(where = {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }) { row ->
            row[RelationshipTable.key] = key
        }
    }

    fun relationshipUpdateName(modelSnapshotId: ModelSnapshotId, relationshipId: RelationshipId, name: LocalizedText?) {
        RelationshipTable.update(where = {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }) { row ->
            row[RelationshipTable.name] = name
        }
    }

    fun relationshipUpdateDescription(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        description: LocalizedMarkdown?
    ) {
        RelationshipTable.update(where = {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }) { row ->
            row[RelationshipTable.description] = description
        }
    }

    fun relationshipRoleInsert(record: RelationshipRoleRecord) {
        RelationshipRoleTable.insert { row ->
            row[RelationshipRoleTable.id] = record.snapshotId
            row[RelationshipRoleTable.lineageId] = record.lineageId
            row[RelationshipRoleTable.relationshipSnapshotId] = record.relationshipSnapshotId
            row[RelationshipRoleTable.key] = record.key
            row[RelationshipRoleTable.entitySnapshotId] = record.entitySnapshotId
            row[RelationshipRoleTable.name] = record.name
            row[RelationshipRoleTable.cardinality] = record.cardinality
        }
    }

    fun relationshipRoleInsert(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        relationshipRoleId: RelationshipRoleId,
        key: RelationshipRoleKey,
        name: LocalizedText?,
        entityId: EntityId,
        cardinality: String
    ) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotIdInModelSnapshot(
            modelSnapshotId,
            relationshipId
        )
        RelationshipRoleTable.insert { row ->
            row[RelationshipRoleTable.id] = RelationshipRoleSnapshotId.generate()
            row[RelationshipRoleTable.lineageId] = relationshipRoleId
            row[RelationshipRoleTable.relationshipSnapshotId] = relationshipSnapshotId
            row[RelationshipRoleTable.key] = key
            row[RelationshipRoleTable.entitySnapshotId] =
                snapshots.currentHeadEntitySnapshotIdInModelSnapshot(modelSnapshotId, entityId)
            row[RelationshipRoleTable.name] = name
            row[RelationshipRoleTable.cardinality] = cardinality
        }
    }

    private fun relationshipRoleUpdate(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        relationshipRoleId: RelationshipRoleId,
        block: (UpdateStatement) -> Unit
    ) {
        val relationshipIds = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }
        RelationshipRoleTable.update(where = {
            (RelationshipRoleTable.lineageId eq relationshipRoleId) and
                    (RelationshipRoleTable.relationshipSnapshotId inSubQuery relationshipIds)
        }) { row ->
            block(row)
        }
    }

    fun relationshipRoleUpdateKey(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        relationshipRoleId: RelationshipRoleId,
        key: RelationshipRoleKey
    ) {
        relationshipRoleUpdate(modelSnapshotId, relationshipId, relationshipRoleId) { row ->
            row[RelationshipRoleTable.key] = key
        }
    }

    fun relationshipRoleUpdateName(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        relationshipRoleId: RelationshipRoleId,
        name: LocalizedText?
    ) {
        relationshipRoleUpdate(modelSnapshotId, relationshipId, relationshipRoleId) { row ->
            row[RelationshipRoleTable.name] = name
        }
    }

    fun relationshipRoleUpdateEntity(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        relationshipRoleId: RelationshipRoleId,
        entityId: EntityId
    ) {
        relationshipRoleUpdate(modelSnapshotId, relationshipId, relationshipRoleId) { row ->
            row[RelationshipRoleTable.entitySnapshotId] =
                snapshots.currentHeadEntitySnapshotIdInModelSnapshot(modelSnapshotId, entityId)
        }
    }

    fun relationshipRoleUpdateCardinality(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        relationshipRoleId: RelationshipRoleId,
        cardinality: String
    ) {
        relationshipRoleUpdate(modelSnapshotId, relationshipId, relationshipRoleId) { row ->
            row[RelationshipRoleTable.cardinality] = cardinality
        }
    }

    fun relationshipRoleDelete(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        relationshipRoleId: RelationshipRoleId
    ) {
        val relationshipIds = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }
        RelationshipRoleTable.deleteWhere {
            (RelationshipRoleTable.lineageId eq relationshipRoleId) and
                    (RelationshipRoleTable.relationshipSnapshotId inSubQuery relationshipIds)
        }
    }

    fun relationshipAddTagIfNotExists(modelSnapshotId: ModelSnapshotId, relationshipId: RelationshipId, tagId: TagId) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotIdInModelSnapshot(
            modelSnapshotId,
            relationshipId
        )
        val exists = RelationshipTagTable.select(RelationshipTagTable.relationshipSnapshotId).where {
            (RelationshipTagTable.relationshipSnapshotId eq relationshipSnapshotId) and
                    (RelationshipTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            relationshipAddTag(relationshipSnapshotId, tagId)
        }
    }

    fun relationshipAddTag(relationshipId: RelationshipSnapshotId, tagId: TagId) {
        RelationshipTagTable.insert { row ->
            row[RelationshipTagTable.relationshipSnapshotId] = relationshipId
            row[RelationshipTagTable.tagId] = tagId
        }
    }

    fun relationshipDeleteTag(modelSnapshotId: ModelSnapshotId, relationshipId: RelationshipId, tagId: TagId) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotIdInModelSnapshot(
            modelSnapshotId,
            relationshipId
        )
        RelationshipTagTable.deleteWhere {
            (RelationshipTagTable.relationshipSnapshotId eq relationshipSnapshotId) and
                    (RelationshipTagTable.tagId eq tagId)
        }
    }

    fun relationshipDelete(modelSnapshotId: ModelSnapshotId, relationshipId: RelationshipId) {
        RelationshipTable.deleteWhere {
            (RelationshipTable.lineageId eq relationshipId) and (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }
    }


    // Relationship attribute
    // ------------------------------------------------------------------------

    fun relationshipAttributeInsert(
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
    private fun relationshipAttributeUpdate(
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

    fun relationshipAttributeUpdateKey(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        key: AttributeKey
    ) {
        relationshipAttributeUpdate(modelSnapshotId, relationshipId, attributeId) { row ->
            row[RelationshipAttributeTable.key] = key
        }
    }

    fun relationshipAttributeUpdateName(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        name: LocalizedText?
    ) {
        relationshipAttributeUpdate(modelSnapshotId, relationshipId, attributeId) { row ->
            row[RelationshipAttributeTable.name] = name
        }
    }

    fun relationshipAttributeUpdateDescription(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        description: LocalizedMarkdown?
    ) {
        relationshipAttributeUpdate(modelSnapshotId, relationshipId, attributeId) { row ->
            row[RelationshipAttributeTable.description] = description
        }
    }

    fun relationshipAttributeUpdateType(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        typeId: TypeId
    ) {
        relationshipAttributeUpdate(modelSnapshotId, relationshipId, attributeId) { row ->
            val typeSnapshotId = snapshots.currentHeadTypeSnapshotIdInModelSnapshot(modelSnapshotId, typeId)
            row[RelationshipAttributeTable.typeSnapshotId] = typeSnapshotId
        }
    }

    fun relationshipAttributeUpdateOptional(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        optional: Boolean
    ) {
        relationshipAttributeUpdate(modelSnapshotId, relationshipId, attributeId) { row ->
            row[RelationshipAttributeTable.optional] = optional
        }
    }

    fun relationshipAttributeAddTag(
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
            relationshipAttributeAddTag(attributeSnapshotId, tagId)
        }

    }

    fun relationshipAttributeAddTag(attributeId: AttributeSnapshotId, tagId: TagId) {
        RelationshipAttributeTagTable.insert { row ->
            row[RelationshipAttributeTagTable.attributeSnapshotId] = attributeId
            row[RelationshipAttributeTagTable.tagId] = tagId
        }
    }

    fun relationshipAttributeDeleteTag(
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

    fun relationshipAttributeDelete(
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


    // Business key
    // -------------------------------------------------------------------------

    fun businessKeyInsert(record: BusinessKeyRecord, attributeSnapshotIds: List<AttributeSnapshotId>) {
        businessKeyInsert(record)
        var priority = DEFAULT_ENTITY_PRIMARY_KEY_PRIORITY
        for (attrId in attributeSnapshotIds) {
            businessKeyAttributeInsert(record.snapshotId, attrId, priority++)
        }
    }
    fun businessKeyInsert(record: BusinessKeyRecord) {
        BusinessKeyTable.insert { row ->
            row[BusinessKeyTable.id] = record.snapshotId
            row[BusinessKeyTable.lineageId] = record.lineageId
            row[BusinessKeyTable.entitySnapshotId] = record.modelEntitySnapshotId
            row[BusinessKeyTable.key] = record.key
            row[BusinessKeyTable.name] = record.name
            row[BusinessKeyTable.description] = record.description
        }
    }

    fun businessKeyAttributeInsert(
        businessKeySnapshotId: BusinessKeySnapshotId,
        attributeSnapshotId: AttributeSnapshotId,
        priority: Int
    ) {
        BusinessKeyAttributeTable.insert { row ->
            row[BusinessKeyAttributeTable.businessKeySnapshotId] = businessKeySnapshotId
            row[BusinessKeyAttributeTable.attributeSnapshotId] = attributeSnapshotId
            row[BusinessKeyAttributeTable.priority] = priority
        }
    }

    companion object {
        private const val DEFAULT_ENTITY_PRIMARY_KEY_PRIORITY = 0
    }

}
