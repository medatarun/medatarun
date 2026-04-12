package io.medatarun.model.infra.db.snapshots

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.tables.*
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

    // Model
    // ------------------------------------------------------------------------

    fun insertModel(record: ModelRecord) {
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

    fun updateModelName(modelSnapshotId: ModelSnapshotId, name: LocalizedText?) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq modelSnapshotId }) { row ->
            row[ModelSnapshotTable.name] = name
        }
    }

    fun updateModelKey(modelSnapshotId: ModelSnapshotId, key: ModelKey) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq modelSnapshotId }) { row ->
            row[ModelSnapshotTable.key] = key
        }
    }

    fun updateModelDescription(modelSnapshotId: ModelSnapshotId, description: LocalizedMarkdown?) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq modelSnapshotId }) { row ->
            row[ModelSnapshotTable.description] = description
        }
    }

    fun updateModelAuthority(modelSnapshotId: ModelSnapshotId, authority: ModelAuthority) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq modelSnapshotId }) { row ->
            row[ModelSnapshotTable.authority] = authority
        }
    }

    fun updateModelDocumentationHome(modelSnapshotId: ModelSnapshotId, documentationHome: String?) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq modelSnapshotId }) { row ->
            row[ModelSnapshotTable.documentationHome] = documentationHome
        }
    }

    fun updateModelVersion(modelSnapshotId: ModelSnapshotId, version: ModelVersion) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq modelSnapshotId }) { row ->
            row[ModelSnapshotTable.version] = version
        }
    }

    fun addModelTag(modelSnapshotId: ModelSnapshotId, tagId: TagId) {
        val exists = ModelTagTable.select(ModelTagTable.modelSnapshotId).where {
            (ModelTagTable.modelSnapshotId eq modelSnapshotId) and (ModelTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertModelTag(modelSnapshotId, tagId)
        }
    }

    fun insertModelTag(modelSnapshotId: ModelSnapshotId, tagId: TagId) {
        ModelTagTable.insert { row ->
            row[ModelTagTable.modelSnapshotId] = modelSnapshotId
            row[ModelTagTable.tagId] = tagId
        }
    }

    fun deleteModelTag(modelSnapshotId: ModelSnapshotId, tagId: TagId) {
        ModelTagTable.deleteWhere {
            (ModelTagTable.modelSnapshotId eq modelSnapshotId) and (ModelTagTable.tagId eq tagId)
        }
    }

    // Types
    // ------------------------------------------------------------------------

    fun insertType(record: ModelTypeRecord) {
        ModelTypeTable.insert { row ->
            row[ModelTypeTable.id] = record.snapshotId
            row[ModelTypeTable.lineageId] = record.lineageId
            row[ModelTypeTable.modelSnapshotId] = record.modelSnapshotId
            row[ModelTypeTable.key] = record.key
            row[ModelTypeTable.name] = record.name
            row[ModelTypeTable.description] = record.description
        }
    }

    fun updateTypeKey(modelSnapshotId: ModelSnapshotId, typeId: TypeId, key: TypeKey) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[ModelTypeTable.key] = key
        }
    }

    fun updateTypeName(modelSnapshotId: ModelSnapshotId, typeId: TypeId, name: LocalizedText?) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[ModelTypeTable.name] = name
        }
    }

    fun updateTypeDescription(modelSnapshotId: ModelSnapshotId, typeId: TypeId, description: LocalizedMarkdown?) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[ModelTypeTable.description] = description
        }
    }

    fun deleteType(modelSnapshotId: ModelSnapshotId, typeId: TypeId) {
        ModelTypeTable.deleteWhere {
            (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
        }
    }

    // Entity
    // ------------------------------------------------------------------------

    fun insertEntity(record: EntityRecord) {
        EntityTable.insert { row ->
            row[EntityTable.id] = record.snapshotId
            row[EntityTable.lineageId] = record.lineageId
            row[EntityTable.modelSnapshotId] = record.modelSnapshotId
            row[EntityTable.key] = record.key
            row[EntityTable.name] = record.name
            row[EntityTable.description] = record.description
            row[EntityTable.identifierAttributeSnapshotId] = record.identifierAttributeSnapshotId
            row[EntityTable.origin] = record.origin
            row[EntityTable.documentationHome] = record.documentationHome
        }
    }

    fun insertEntityPrimaryKey(record: EntityPKRecord) {
        EntityPKTable.insert { row ->
            row[EntityPKTable.id] = record.snapshotId
            row[EntityPKTable.lineageId] = record.lineageId
            row[EntityPKTable.entitySnapshotId] = record.modelEntitySnapshotId
        }
    }

    fun insertEntityPrimaryKeyAttribute(
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

    fun insertBusinessKey(record: BusinessKeyRecord) {
        BusinessKeyTable.insert { row ->
            row[BusinessKeyTable.id] = record.snapshotId
            row[BusinessKeyTable.lineageId] = record.lineageId
            row[BusinessKeyTable.entitySnapshotId] = record.modelEntitySnapshotId
            row[BusinessKeyTable.key] = record.key
            row[BusinessKeyTable.name] = record.name
            row[BusinessKeyTable.description] = record.description
        }
    }

    fun insertBusinessKeyAttribute(
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

    fun updateEntityKey(modelSnapshotId: ModelSnapshotId, entityId: EntityId, key: EntityKey) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[EntityTable.key] = key
        }
    }

    fun updateEntityName(modelSnapshotId: ModelSnapshotId, entityId: EntityId, name: LocalizedText?) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[EntityTable.name] = name
        }
    }

    fun updateEntityDescription(
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

    fun updateEntityIdentifierAttribute(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        identifierAttributeId: AttributeId
    ) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(modelSnapshotId, entityId)
        val attributeSnapshotId = EntityAttributeTable.select(EntityAttributeTable.id).where {
            (EntityAttributeTable.lineageId eq identifierAttributeId) and
                    (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
        }.single()[EntityAttributeTable.id]
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }
        ) { row ->
            row[EntityTable.identifierAttributeSnapshotId] = attributeSnapshotId
        }
    }

    fun updateEntityDocumentationHome(
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

    fun addEntityTag(modelSnapshotId: ModelSnapshotId, entityId: EntityId, tagId: TagId) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(modelSnapshotId, entityId)
        val exists = EntityTagTable.select(EntityTagTable.entitySnapshotId).where {
            (EntityTagTable.entitySnapshotId eq entitySnapshotId) and (EntityTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertEntityTag(entitySnapshotId, tagId)
        }
    }

    fun insertEntityTag(entityId: EntitySnapshotId, tagId: TagId) {
        EntityTagTable.insert { row ->
            row[EntityTagTable.entitySnapshotId] = entityId
            row[EntityTagTable.tagId] = tagId
        }
    }

    fun deleteEntityTag(modelSnapshotId: ModelSnapshotId, entityId: EntityId, tagId: TagId) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(modelSnapshotId, entityId)
        EntityTagTable.deleteWhere {
            (EntityTagTable.entitySnapshotId eq entitySnapshotId) and (EntityTagTable.tagId eq tagId)
        }
    }

    fun deleteEntity(modelSnapshotId: ModelSnapshotId, entityId: EntityId) {
        EntityTable.deleteWhere {
            (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
        }
    }

    // Entity attribute
    // ------------------------------------------------------------------------

    fun insertEntityAttribute(record: EntityAttributeRecord) {
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

    private fun updateEntityAttribute(
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

    fun updateEntityAttributeKey(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        key: AttributeKey
    ) {
        updateEntityAttribute(modelSnapshotId, entityId, attributeId) { row ->
            row[EntityAttributeTable.key] = key
        }
    }

    fun updateEntityAttributeName(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        name: LocalizedText?
    ) {
        updateEntityAttribute(modelSnapshotId, entityId, attributeId) { row ->
            row[EntityAttributeTable.name] = name
        }
    }

    fun updateEntityAttributeDescription(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        description: LocalizedMarkdown?
    ) {
        updateEntityAttribute(modelSnapshotId, entityId, attributeId) { row ->
            row[EntityAttributeTable.description] = description
        }
    }

    fun updateEntityAttributeType(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        typeId: TypeId
    ) {
        updateEntityAttribute(modelSnapshotId, entityId, attributeId) { row ->
            row[EntityAttributeTable.typeSnapshotId] =
                snapshots.currentHeadTypeSnapshotIdInModelSnapshot(modelSnapshotId, typeId)
        }
    }

    fun updateEntityAttributeOptional(
        modelSnapshotId: ModelSnapshotId,
        entityId: EntityId,
        attributeId: AttributeId,
        optional: Boolean
    ) {
        updateEntityAttribute(modelSnapshotId, entityId, attributeId) { row ->
            row[EntityAttributeTable.optional] = optional
        }
    }

    fun addEntityAttributeTag(
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
            insertEntityAttributeTag(attributeSnapshotId, tagId)
        }
    }

    fun insertEntityAttributeTag(attributeId: AttributeSnapshotId, tagId: TagId) {
        EntityAttributeTagTable.insert { row ->
            row[EntityAttributeTagTable.attributeSnapshotId] = attributeId
            row[EntityAttributeTagTable.tagId] = tagId
        }
    }

    fun deleteEntityAttributeTag(
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

    fun deleteEntityAttribute(
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

    fun insertRelationship(record: RelationshipRecord, roles: List<RelationshipRoleRecord>) {
        RelationshipTable.insert { row ->
            row[RelationshipTable.id] = record.snapshotId
            row[RelationshipTable.lineageId] = record.lineageId
            row[RelationshipTable.modelSnapshotId] = record.modelSnapshotId
            row[RelationshipTable.key] = record.key
            row[RelationshipTable.name] = record.name
            row[RelationshipTable.description] = record.description
        }
        for (roleRecord in roles) {
            insertRelationshipRole(roleRecord)
        }
    }

    fun insertRelationshipRole(record: RelationshipRoleRecord) {
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

    fun updateRelationshipKey(modelSnapshotId: ModelSnapshotId, relationshipId: RelationshipId, key: RelationshipKey) {
        RelationshipTable.update(where = {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }) { row ->
            row[RelationshipTable.key] = key
        }
    }

    fun updateRelationshipName(modelSnapshotId: ModelSnapshotId, relationshipId: RelationshipId, name: LocalizedText?) {
        RelationshipTable.update(where = {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }) { row ->
            row[RelationshipTable.name] = name
        }
    }

    fun updateRelationshipDescription(
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

    fun createRelationshipRole(
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

    private fun updateRelationshipRole(
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

    fun updateRelationshipRoleKey(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        relationshipRoleId: RelationshipRoleId,
        key: RelationshipRoleKey
    ) {
        updateRelationshipRole(modelSnapshotId, relationshipId, relationshipRoleId) { row ->
            row[RelationshipRoleTable.key] = key
        }
    }

    fun updateRelationshipRoleName(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        relationshipRoleId: RelationshipRoleId,
        name: LocalizedText?
    ) {
        updateRelationshipRole(modelSnapshotId, relationshipId, relationshipRoleId) { row ->
            row[RelationshipRoleTable.name] = name
        }
    }

    fun updateRelationshipRoleEntity(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        relationshipRoleId: RelationshipRoleId,
        entityId: EntityId
    ) {
        updateRelationshipRole(modelSnapshotId, relationshipId, relationshipRoleId) { row ->
            row[RelationshipRoleTable.entitySnapshotId] =
                snapshots.currentHeadEntitySnapshotIdInModelSnapshot(modelSnapshotId, entityId)
        }
    }

    fun updateRelationshipRoleCardinality(
        modelSnapshotId: ModelSnapshotId,
        relationshipId: RelationshipId,
        relationshipRoleId: RelationshipRoleId,
        cardinality: String
    ) {
        updateRelationshipRole(modelSnapshotId, relationshipId, relationshipRoleId) { row ->
            row[RelationshipRoleTable.cardinality] = cardinality
        }
    }

    fun deleteRelationshipRole(
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

    fun addRelationshipTag(modelSnapshotId: ModelSnapshotId, relationshipId: RelationshipId, tagId: TagId) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotIdInModelSnapshot(
            modelSnapshotId,
            relationshipId
        )
        val exists = RelationshipTagTable.select(RelationshipTagTable.relationshipSnapshotId).where {
            (RelationshipTagTable.relationshipSnapshotId eq relationshipSnapshotId) and
                    (RelationshipTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertRelationshipTag(relationshipSnapshotId, tagId)
        }
    }

    fun insertRelationshipTag(relationshipId: RelationshipSnapshotId, tagId: TagId) {
        RelationshipTagTable.insert { row ->
            row[RelationshipTagTable.relationshipSnapshotId] = relationshipId
            row[RelationshipTagTable.tagId] = tagId
        }
    }

    fun deleteRelationship(modelSnapshotId: ModelSnapshotId, relationshipId: RelationshipId) {
        RelationshipTable.deleteWhere {
            (RelationshipTable.lineageId eq relationshipId) and (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }
    }

    fun deleteRelationshipTag(modelSnapshotId: ModelSnapshotId, relationshipId: RelationshipId, tagId: TagId) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotIdInModelSnapshot(
            modelSnapshotId,
            relationshipId
        )
        RelationshipTagTable.deleteWhere {
            (RelationshipTagTable.relationshipSnapshotId eq relationshipSnapshotId) and
                    (RelationshipTagTable.tagId eq tagId)
        }
    }

    // Relationship attribute
    // ------------------------------------------------------------------------

    private fun updateRelationshipAttribute(
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
