package io.medatarun.model.infra.db.snapshots

import io.medatarun.model.domain.*
import io.medatarun.model.infra.db.ModelStorageDbMissingCurrentHeadModelSnapshotException
import io.medatarun.model.infra.db.ModelStorageDbSearchWrite
import io.medatarun.model.infra.db.ModelStorageDbUnsupportedProjectedDeleteException
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.tables.*
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.ports.needs.ModelClock
import io.medatarun.model.ports.needs.ModelStorageCmd
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.jdbc.*

internal class ModelStorageDbProjection(
    private val searchWrite: ModelStorageDbSearchWrite,
    private val snapshots: ModelStorageDbSnapshots,
    private val clock: ModelClock
) {

    data class ProjectionEventCtx(
        val cmd: ModelStorageCmd,
        val modelId: ModelId,
        val modelSnapshotId: ModelSnapshotId,
        val modelEventId: ModelEventId,
        val streamRevision: Int
    )

    fun projectCommand(ctx: ProjectionEventCtx) {
        when (val cmd = ctx.cmd) {
            //@formatter:off
            is ModelStorageCmd.StoreModelAggregate -> storeModelAggregate(ctx, cmd)
            is ModelStorageCmd.CreateModel -> createModel(ctx, cmd)
            is ModelStorageCmd.UpdateModelName -> updateModelName(ctx, cmd)
            is ModelStorageCmd.UpdateModelKey -> updateModelKey(ctx, cmd)
            is ModelStorageCmd.UpdateModelDescription -> updateModelDescription(ctx, cmd)
            is ModelStorageCmd.UpdateModelAuthority -> updateModelAuthority(ctx, cmd)
            is ModelStorageCmd.ModelRelease -> releaseModel(ctx, cmd)
            is ModelStorageCmd.UpdateModelDocumentationHome -> updateModelDocumentationHome(ctx, cmd)
            is ModelStorageCmd.UpdateModelTagAdd -> addModelTag(ctx, cmd)
            is ModelStorageCmd.UpdateModelTagDelete -> deleteModelTag(ctx, cmd)
            is ModelStorageCmd.CreateType -> createType(ctx, cmd)
            is ModelStorageCmd.UpdateTypeKey -> updateTypeKey(ctx, cmd)
            is ModelStorageCmd.UpdateTypeName -> updateTypeName(ctx, cmd)
            is ModelStorageCmd.UpdateTypeDescription -> updateTypeDescription(ctx, cmd)
            is ModelStorageCmd.DeleteType -> deleteType(ctx, cmd)
            is ModelStorageCmd.CreateEntity -> createEntity(ctx, cmd)
            is ModelStorageCmd.UpdateEntityKey -> updateEntityKey(ctx, cmd)
            is ModelStorageCmd.UpdateEntityName -> updateEntityName(ctx, cmd)
            is ModelStorageCmd.UpdateEntityDescription -> updateEntityDescription(ctx, cmd)
            is ModelStorageCmd.UpdateEntityIdentifierAttribute -> updateEntityIdentifierAttribute(ctx, cmd)
            is ModelStorageCmd.UpdateEntityDocumentationHome -> updateEntityDocumentationHome(ctx, cmd)
            is ModelStorageCmd.UpdateEntityTagAdd -> addEntityTag(ctx, cmd)
            is ModelStorageCmd.UpdateEntityTagDelete -> deleteEntityTag(ctx, cmd)
            is ModelStorageCmd.DeleteEntity -> deleteEntity(ctx, cmd)
            is ModelStorageCmd.CreateEntityAttribute -> createEntityAttribute(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeKey -> updateEntityAttributeKey(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeName -> updateEntityAttributeName(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeDescription -> updateEntityAttributeDescription(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeType -> updateEntityAttributeType(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeOptional -> updateEntityAttributeOptional(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeTagAdd -> addEntityAttributeTag(ctx, cmd)
            is ModelStorageCmd.UpdateEntityAttributeTagDelete -> deleteEntityAttributeTag(ctx, cmd)
            is ModelStorageCmd.DeleteEntityAttribute -> deleteEntityAttribute(ctx, cmd)
            is ModelStorageCmd.CreateRelationship -> createRelationship(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipKey -> updateRelationshipKey(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipName -> updateRelationshipName(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipDescription -> updateRelationshipDescription(ctx, cmd)
            is ModelStorageCmd.CreateRelationshipRole -> createRelationshipRole(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipRoleKey -> updateRelationshipRoleKey(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipRoleName -> updateRelationshipRoleName(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipRoleEntity -> updateRelationshipRoleEntity(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipRoleCardinality -> updateRelationshipRoleCardinality(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipTagAdd -> addRelationshipTag(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipTagDelete -> deleteRelationshipTag(ctx, cmd)
            is ModelStorageCmd.DeleteRelationship -> deleteRelationship(ctx, cmd)
            is ModelStorageCmd.DeleteRelationshipRole -> deleteRelationshipRole(ctx, cmd)
            is ModelStorageCmd.CreateRelationshipAttribute -> createRelationshipAttribute(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeKey -> updateRelationshipAttributeKey(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeName -> updateRelationshipAttributeName(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeDescription -> updateRelationshipAttributeDescription(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeType -> updateRelationshipAttributeType(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeOptional -> updateRelationshipAttributeOptional(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeTagAdd -> addRelationshipAttributeTag(ctx, cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeTagDelete -> deleteRelationshipAttributeTag(ctx, cmd)
            is ModelStorageCmd.DeleteRelationshipAttribute -> deleteRelationshipAttribute(ctx, cmd)
            is ModelStorageCmd.DeleteModel -> throw ModelStorageDbUnsupportedProjectedDeleteException("model_deleted")
            //@formatter:on
        }
    }

    // Model
    // ------------------------------------------------------------------------


    private fun createModel(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateModel) {
        val inMemoryModel = ModelInMemory(
            id = cmd.id,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description,
            version = cmd.version,
            origin = cmd.origin,
            authority = cmd.authority,
            documentationHome = cmd.documentationHome,
        )
        insertModel(ctx.modelSnapshotId, inMemoryModel)
        searchWrite.upsertModelSearchItem(ctx.modelSnapshotId)
    }

    private fun storeModelAggregate(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.StoreModelAggregate) {

        val modelSnapshotId = insertModel(
            ctx.modelSnapshotId,
            ModelInMemory(
                id = cmd.model.id,
                key = cmd.model.key,
                name = cmd.model.name,
                description = cmd.model.description,
                version = cmd.model.version,
                origin = cmd.model.origin,
                authority = cmd.model.authority,
                documentationHome = cmd.model.documentationHome,
            )
        )

        val typeSnapshotIds = mutableMapOf<TypeId, TypeSnapshotId>()
        val entitySnapshotIds = mutableMapOf<EntityId, EntitySnapshotId>()
        val entityAttributeSnapshotIds = mutableMapOf<AttributeId, AttributeSnapshotId>()
        val relationshipSnapshotIds = mutableMapOf<RelationshipId, RelationshipSnapshotId>()
        val relationshipAttributeSnapshotIds = mutableMapOf<AttributeId, AttributeSnapshotId>()

        for (type in cmd.types) {
            val typeSnapshotId = TypeSnapshotId.generate()
            typeSnapshotIds[type.id] = typeSnapshotId
            insertType(
                ModelTypeRecord(
                    snapshotId = typeSnapshotId,
                    lineageId = type.id,
                    modelSnapshotId = modelSnapshotId,
                    key = type.key,
                    name = type.name,
                    description = type.description
                )
            )
        }

        for (entity in cmd.entities) {
            val entitySnapshotId = EntitySnapshotId.generate()
            entitySnapshotIds[entity.id] = entitySnapshotId
            val identifierAttributeSnapshotId = entityAttributeSnapshotIds.getOrPut(entity.identifierAttributeId) {
                AttributeSnapshotId.generate()
            }
            insertEntity(
                EntityRecord(
                    snapshotId = entitySnapshotId,
                    lineageId = entity.id,
                    modelSnapshotId = modelSnapshotId,
                    key = entity.key,
                    name = entity.name,
                    description = entity.description,
                    identifierAttributeSnapshotId = identifierAttributeSnapshotId,
                    origin = entity.origin,
                    documentationHome = entity.documentationHome?.toExternalForm(),
                )
            )
            searchWrite.upsertEntitySearchItem(modelSnapshotId, entity.id)

            for (attr in cmd.entityAttributes.filter { it.entityId == entity.id }) {
                val attributeSnapshotId = entityAttributeSnapshotIds.getOrPut(attr.id) {
                    AttributeSnapshotId.generate()
                }
                insertEntityAttribute(
                    EntityAttributeRecord(
                        snapshotId = attributeSnapshotId,
                        lineageId = attr.id,
                        entitySnapshotId = entitySnapshotId,
                        key = attr.key,
                        name = attr.name,
                        description = attr.description,
                        typeSnapshotId = typeSnapshotIds.getValue(attr.typeId),
                        optional = attr.optional
                    )
                )
                searchWrite.upsertEntityAttributeSearchItem(modelSnapshotId, entity.id, attr.id)
            }
        }

        for (relationship in cmd.relationships) {
            val relationshipSnapshotId = RelationshipSnapshotId.generate()
            relationshipSnapshotIds[relationship.id] = relationshipSnapshotId
            insertRelationship(
                record = RelationshipRecord(
                    snapshotId = relationshipSnapshotId,
                    lineageId = relationship.id,
                    modelSnapshotId = modelSnapshotId,
                    key = relationship.key,
                    name = relationship.name,
                    description = relationship.description
                ),
                roles = relationship.roles.map { role ->
                    RelationshipRoleRecord(
                        snapshotId = RelationshipRoleSnapshotId.generate(),
                        lineageId = role.id,
                        relationshipSnapshotId = relationshipSnapshotId,
                        key = role.key,
                        entitySnapshotId = entitySnapshotIds.getValue(role.entityId),
                        name = role.name,
                        cardinality = role.cardinality.code
                    )
                }
            )
            searchWrite.upsertRelationshipSearchItem(modelSnapshotId, relationship.id)

            for (attr in cmd.relationshipAttributes.filter { it.relationshipId == relationship.id }) {
                val attributeSnapshotId = relationshipAttributeSnapshotIds.getOrPut(attr.id) {
                    AttributeSnapshotId.generate()
                }
                insertRelationshipAttribute(
                    RelationshipAttributeRecord(
                        snapshotId = attributeSnapshotId,
                        lineageId = attr.id,
                        relationshipSnapshotId = relationshipSnapshotId,
                        key = attr.key,
                        name = attr.name,
                        description = attr.description,
                        typeSnapshotId = typeSnapshotIds.getValue(attr.typeId),
                        optional = attr.optional
                    )
                )
                searchWrite.upsertRelationshipAttributeSearchItem(modelSnapshotId, relationship.id, attr.id)
            }
        }

        searchWrite.upsertModelSearchItem(modelSnapshotId)

    }


    private fun updateModelName(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateModelName) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq ctx.modelSnapshotId }) { row ->
            row[ModelSnapshotTable.name] = cmd.name
        }
        searchWrite.upsertModelSearchItem(ctx.modelSnapshotId)
    }

    private fun updateModelKey(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateModelKey) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq ctx.modelSnapshotId }) { row ->
            row[ModelSnapshotTable.key] = cmd.key
        }
        searchWrite.upsertModelSearchItem(ctx.modelSnapshotId)
    }

    private fun updateModelDescription(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateModelDescription) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq ctx.modelSnapshotId }) { row ->
            row[ModelSnapshotTable.description] = cmd.description
        }
        searchWrite.upsertModelSearchItem(ctx.modelSnapshotId)
    }

    private fun updateModelAuthority(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateModelAuthority) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq ctx.modelSnapshotId }) { row ->
            row[ModelSnapshotTable.authority] = cmd.authority
        }
    }

    private fun releaseModel(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.ModelRelease) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq ctx.modelSnapshotId }) { row ->
            row[ModelSnapshotTable.version] = cmd.version
        }
        createVersionSnapshotFromCurrentHead(ctx, cmd.version)
    }

    private fun updateModelDocumentationHome(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateModelDocumentationHome
    ) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.id eq ctx.modelSnapshotId }) { row ->
            row[ModelSnapshotTable.documentationHome] = cmd.documentationHome?.toExternalForm()
        }
    }

    private fun addModelTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateModelTagAdd) {
        val exists = ModelTagTable.select(ModelTagTable.modelSnapshotId).where {
            (ModelTagTable.modelSnapshotId eq ctx.modelSnapshotId) and (ModelTagTable.tagId eq cmd.tagId)
        }.limit(1).any()
        if (!exists) {
            ModelTagTable.insert { row ->
                row[ModelTagTable.modelSnapshotId] = ctx.modelSnapshotId
                row[ModelTagTable.tagId] = cmd.tagId
            }
        }
        searchWrite.upsertModelSearchItem(ctx.modelSnapshotId)
    }

    private fun deleteModelTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateModelTagDelete) {
        ModelTagTable.deleteWhere {
            (ModelTagTable.modelSnapshotId eq ctx.modelSnapshotId) and (ModelTagTable.tagId eq cmd.tagId)
        }
        searchWrite.upsertModelSearchItem(ctx.modelSnapshotId)
    }

    private fun insertModel(modelSnapshotId: ModelSnapshotId, model: Model): ModelSnapshotId {
        ModelSnapshotTable.insert { row ->
            row[ModelSnapshotTable.id] = modelSnapshotId
            row[ModelSnapshotTable.modelId] = model.id
            row[ModelSnapshotTable.key] = model.key
            row[ModelSnapshotTable.name] = model.name
            row[ModelSnapshotTable.description] = model.description
            row[ModelSnapshotTable.origin] = model.origin
            row[ModelSnapshotTable.authority] = model.authority
            row[ModelSnapshotTable.documentationHome] = model.documentationHome?.toExternalForm()
            row[ModelSnapshotTable.snapshotKind] = ModelSnapshotKind.CURRENT_HEAD
            row[ModelSnapshotTable.upToRevision] = 0
            row[ModelSnapshotTable.modelEventReleaseId] = null
            row[ModelSnapshotTable.version] = model.version
            row[ModelSnapshotTable.createdAt] = clock.now().toString()
            row[ModelSnapshotTable.updatedAt] = clock.now().toString()
        }
        return modelSnapshotId
    }

    /**
     * Creates a frozen VERSION_SNAPSHOT by cloning the current head rows and
     * remapping every internal snapshot reference to fresh snapshot ids.
     */
    private fun createVersionSnapshotFromCurrentHead(ctx: ProjectionEventCtx, version: ModelVersion) {
        val currentHeadSnapshotId = ctx.modelSnapshotId
        val currentHeadRow =
            ModelSnapshotTable.selectAll().where { ModelSnapshotTable.id eq currentHeadSnapshotId }.singleOrNull()
                ?: throw ModelStorageDbMissingCurrentHeadModelSnapshotException(ctx.modelId)
        val currentHeadRecord = ModelRecord.read(currentHeadRow)
        val versionSnapshotId = ModelSnapshotId.generate()
        val now = clock.now().toString()

        ModelSnapshotTable.insert { row ->
            row[ModelSnapshotTable.id] = versionSnapshotId
            row[ModelSnapshotTable.modelId] = currentHeadRecord.modelId
            row[ModelSnapshotTable.key] = currentHeadRecord.key
            row[ModelSnapshotTable.name] = currentHeadRecord.name
            row[ModelSnapshotTable.description] = currentHeadRecord.description
            row[ModelSnapshotTable.origin] = currentHeadRecord.origin
            row[ModelSnapshotTable.authority] = currentHeadRecord.authority
            row[ModelSnapshotTable.documentationHome] = currentHeadRecord.documentationHome
            row[ModelSnapshotTable.snapshotKind] = ModelSnapshotKind.VERSION_SNAPSHOT
            row[ModelSnapshotTable.upToRevision] = ctx.streamRevision
            row[ModelSnapshotTable.modelEventReleaseId] = ctx.modelEventId
            row[ModelSnapshotTable.version] = version
            row[ModelSnapshotTable.createdAt] = now
            row[ModelSnapshotTable.updatedAt] = now
        }

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
            ModelTagTable.insert { row ->
                row[ModelTagTable.modelSnapshotId] = versionSnapshotId
                row[ModelTagTable.tagId] = tagId
            }
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
            insertType(
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
            insertEntity(
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
            insertEntityAttribute(
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
                insertEntityTag(entry.value, tagId)
            }
        }
    }

    private fun cloneEntityAttributeTags(attributeSnapshotIdMap: Map<AttributeSnapshotId, AttributeSnapshotId>) {
        for (entry in attributeSnapshotIdMap.entries) {
            val tagIds = EntityAttributeTagTable.select(EntityAttributeTagTable.tagId)
                .where { EntityAttributeTagTable.attributeSnapshotId eq entry.key }
                .map { it[EntityAttributeTagTable.tagId] }
            for (tagId in tagIds) {
                insertEntityAttributeTag(entry.value, tagId)
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
            insertRelationship(
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
                insertRelationshipTag(entry.value, tagId)
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
            RelationshipRoleTable.insert { insertRow ->
                insertRow[RelationshipRoleTable.id] = RelationshipRoleSnapshotId.generate()
                insertRow[RelationshipRoleTable.lineageId] = record.lineageId
                insertRow[RelationshipRoleTable.relationshipSnapshotId] =
                    relationshipSnapshotIdMap.getValue(record.relationshipSnapshotId)
                insertRow[RelationshipRoleTable.key] = record.key
                insertRow[RelationshipRoleTable.entitySnapshotId] =
                    entitySnapshotIdMap.getValue(record.entitySnapshotId)
                insertRow[RelationshipRoleTable.name] = record.name
                insertRow[RelationshipRoleTable.cardinality] = record.cardinality
            }
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
            insertRelationshipAttribute(
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
                insertRelationshipAttributeTag(entry.value, tagId)
            }
        }
    }


    // Types
    // ------------------------------------------------------------------------


    private fun createType(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateType) {
        val lineageId = TypeId.generate()
        val record = ModelTypeRecord(
            snapshotId = TypeSnapshotId.generate(),
            lineageId = lineageId,
            modelSnapshotId = ctx.modelSnapshotId,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description
        )
        insertType(record)
    }

    private fun insertType(record: ModelTypeRecord) {
        ModelTypeTable.insert { row ->
            row[ModelTypeTable.id] = record.snapshotId
            row[ModelTypeTable.lineageId] = record.lineageId
            row[ModelTypeTable.modelSnapshotId] = record.modelSnapshotId
            row[ModelTypeTable.key] = record.key
            row[ModelTypeTable.name] = record.name
            row[ModelTypeTable.description] = record.description
        }
    }

    private fun updateTypeKey(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateTypeKey) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq cmd.typeId) and (ModelTypeTable.modelSnapshotId eq ctx.modelSnapshotId)
            }) { row ->
            row[ModelTypeTable.key] = cmd.key
        }
    }

    private fun updateTypeName(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateTypeName) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq cmd.typeId) and (ModelTypeTable.modelSnapshotId eq ctx.modelSnapshotId)
            }) { row ->
            row[ModelTypeTable.name] = cmd.name
        }
    }

    private fun updateTypeDescription(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateTypeDescription) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq cmd.typeId) and (ModelTypeTable.modelSnapshotId eq ctx.modelSnapshotId)
            }) { row ->
            row[ModelTypeTable.description] = cmd.description
        }
    }

    private fun deleteType(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.DeleteType) {
        ModelTypeTable.deleteWhere {
            (ModelTypeTable.lineageId eq cmd.typeId) and (ModelTypeTable.modelSnapshotId eq ctx.modelSnapshotId)
        }
    }

    // Entity
    // ------------------------------------------------------------------------


    private fun createEntity(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateEntity) {
        val entitySnapshotId = EntitySnapshotId.generate()
        val identifierAttributeSnapshotId = AttributeSnapshotId.generate()
        val record = EntityRecord(
            snapshotId = entitySnapshotId,
            lineageId = cmd.entityId,
            modelSnapshotId = ctx.modelSnapshotId,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description,
            identifierAttributeSnapshotId = identifierAttributeSnapshotId,
            origin = cmd.origin,
            documentationHome = cmd.documentationHome?.toExternalForm()
        )
        insertEntity(record)
        insertEntityAttribute(
            EntityAttributeRecord(
                snapshotId = identifierAttributeSnapshotId,
                lineageId = cmd.identityAttributeId,
                entitySnapshotId = entitySnapshotId,
                key = cmd.identityAttributeKey,
                name = cmd.identityAttributeName,
                description = cmd.identityAttributeDescription,
                typeSnapshotId = snapshots.currentHeadTypeSnapshotIdInModelSnapshot(
                    ctx.modelSnapshotId,
                    cmd.identityAttributeTypeId
                ),
                optional = cmd.identityAttributeIdOptional
            )
        )
        searchWrite.upsertEntitySearchItem(ctx.modelSnapshotId, cmd.entityId)
        searchWrite.upsertEntityAttributeSearchItem(ctx.modelSnapshotId, cmd.entityId, cmd.identityAttributeId)
    }

    private fun updateEntityKey(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityKey) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq ctx.modelSnapshotId)
            }) { row ->
            row[EntityTable.key] = cmd.key
        }
        searchWrite.upsertEntitySearchItem(ctx.modelSnapshotId, cmd.entityId)
    }

    private fun updateEntityName(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityName) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq ctx.modelSnapshotId)
            }) { row ->
            row[EntityTable.name] = cmd.name
        }
        searchWrite.upsertEntitySearchItem(ctx.modelSnapshotId, cmd.entityId)
    }

    private fun updateEntityDescription(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityDescription) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq ctx.modelSnapshotId)
            }) { row ->
            row[EntityTable.description] = cmd.description
        }
        searchWrite.upsertEntitySearchItem(ctx.modelSnapshotId, cmd.entityId)
    }

    private fun updateEntityIdentifierAttribute(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateEntityIdentifierAttribute
    ) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.entityId)
        val attributeSnapshotId = EntityAttributeTable.select(EntityAttributeTable.id).where {
            (EntityAttributeTable.lineageId eq cmd.identifierAttributeId) and
                    (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
        }.single()[EntityAttributeTable.id]
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq ctx.modelSnapshotId)
            }) { row ->
            row[EntityTable.identifierAttributeSnapshotId] = attributeSnapshotId
        }
    }

    private fun updateEntityDocumentationHome(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateEntityDocumentationHome
    ) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq ctx.modelSnapshotId)
            }) { row ->
            row[EntityTable.documentationHome] = cmd.documentationHome?.toExternalForm()
        }
    }

    private fun addEntityTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityTagAdd) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.entityId)
        val exists = EntityTagTable.select(EntityTagTable.entitySnapshotId).where {
            (EntityTagTable.entitySnapshotId eq entitySnapshotId) and (EntityTagTable.tagId eq cmd.tagId)
        }.limit(1).any()
        if (!exists) {
            insertEntityTag(entitySnapshotId, cmd.tagId)
        }
        searchWrite.upsertEntitySearchItem(ctx.modelSnapshotId, cmd.entityId)
    }

    private fun insertEntityTag(entityId: EntitySnapshotId, tagId: TagId) {
        EntityTagTable.insert { row ->
            row[EntityTagTable.entitySnapshotId] = entityId
            row[EntityTagTable.tagId] = tagId
        }
    }

    private fun deleteEntityTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityTagDelete) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.entityId)
        EntityTagTable.deleteWhere {
            (EntityTagTable.entitySnapshotId eq entitySnapshotId) and (EntityTagTable.tagId eq cmd.tagId)
        }
        searchWrite.upsertEntitySearchItem(ctx.modelSnapshotId, cmd.entityId)
    }

    private fun deleteEntity(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.DeleteEntity) {
        searchWrite.deleteEntityBranch(cmd.entityId)
        EntityTable.deleteWhere {
            (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq ctx.modelSnapshotId)
        }
    }

    private fun insertEntity(record: EntityRecord) {
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

    // Entity attribute
    // ------------------------------------------------------------------------


    private fun createEntityAttribute(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateEntityAttribute) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.entityId)
        insertEntityAttribute(
            EntityAttributeRecord(
                snapshotId = AttributeSnapshotId.generate(),
                lineageId = cmd.attributeId,
                entitySnapshotId = entitySnapshotId,
                key = cmd.key,
                name = cmd.name,
                description = cmd.description,
                typeSnapshotId = snapshots.currentHeadTypeSnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.typeId),
                optional = cmd.optional
            )
        )
        searchWrite.upsertEntityAttributeSearchItem(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }
    private fun updateEntityAttribute(
        ctx: ProjectionEventCtx,
        entityId: EntityId,
        attributeId: AttributeId,
        block: (UpdateStatement) -> Unit
    ) {
        val entityIds = EntityTable.select(EntityTable.id).where {
            (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq ctx.modelSnapshotId)
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

    private fun updateEntityAttributeKey(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityAttributeKey) {
        updateEntityAttribute(ctx, cmd.entityId, cmd.attributeId) { row ->
            row[EntityAttributeTable.key] = cmd.key
        }
        searchWrite.upsertEntityAttributeSearchItem(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }

    private fun updateEntityAttributeName(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityAttributeName) {
        updateEntityAttribute(ctx, cmd.entityId, cmd.attributeId) { row ->
            row[EntityAttributeTable.name] = cmd.name
        }
        searchWrite.upsertEntityAttributeSearchItem(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }

    private fun updateEntityAttributeDescription(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateEntityAttributeDescription
    ) {
        updateEntityAttribute(ctx, cmd.entityId, cmd.attributeId) { row ->
            row[EntityAttributeTable.description] = cmd.description
        }
        searchWrite.upsertEntityAttributeSearchItem(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }

    private fun updateEntityAttributeType(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityAttributeType) {
        updateEntityAttribute(ctx, cmd.entityId, cmd.attributeId) { row ->
            row[EntityAttributeTable.typeSnapshotId] =
                snapshots.currentHeadTypeSnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.typeId)
        }
    }

    private fun updateEntityAttributeOptional(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateEntityAttributeOptional
    ) {
        updateEntityAttribute(ctx, cmd.entityId, cmd.attributeId) { row ->
            row[EntityAttributeTable.optional] = cmd.optional
        }
    }


    private fun insertEntityAttribute(record: EntityAttributeRecord) {
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


    private fun addEntityAttributeTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityAttributeTagAdd) {
        val entityIds = EntityTable.select(EntityTable.id).where {
            (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq ctx.modelSnapshotId)
        }
        val attributeSnapshotId = EntityAttributeTable.select(EntityAttributeTable.id).where {
            (EntityAttributeTable.lineageId eq cmd.attributeId) and
                    (EntityAttributeTable.entitySnapshotId inSubQuery entityIds)
        }
            .single()[EntityAttributeTable.id]
        val exists = EntityAttributeTagTable.select(EntityAttributeTagTable.attributeSnapshotId).where {
            (EntityAttributeTagTable.attributeSnapshotId eq attributeSnapshotId) and (EntityAttributeTagTable.tagId eq cmd.tagId)
        }.limit(1).any()
        if (!exists) {
            insertEntityAttributeTag(attributeSnapshotId, cmd.tagId)
        }
        searchWrite.upsertEntityAttributeSearchItem(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }

    private fun insertEntityAttributeTag(attributeId: AttributeSnapshotId, tagId: TagId) {
        EntityAttributeTagTable.insert { row ->
            row[EntityAttributeTagTable.attributeSnapshotId] = attributeId
            row[EntityAttributeTagTable.tagId] = tagId
        }
    }

    private fun deleteEntityAttributeTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateEntityAttributeTagDelete) {
        val entityIds = EntityTable.select(EntityTable.id).where {
            (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq ctx.modelSnapshotId)
        }
        val attributeIds = EntityAttributeTable.select(EntityAttributeTable.id).where {
            (EntityAttributeTable.lineageId eq cmd.attributeId) and
                    (EntityAttributeTable.entitySnapshotId inSubQuery entityIds)
        }
        EntityAttributeTagTable.deleteWhere {
            (EntityAttributeTagTable.attributeSnapshotId inSubQuery attributeIds) and
                    (EntityAttributeTagTable.tagId eq cmd.tagId)
        }
        searchWrite.upsertEntityAttributeSearchItem(ctx.modelSnapshotId, cmd.entityId, cmd.attributeId)
    }

    private fun deleteEntityAttribute(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.DeleteEntityAttribute) {
        searchWrite.deleteEntityAttributeSearchItem(cmd.attributeId)
        val entityIds = EntityTable.select(EntityTable.id).where {
            (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq ctx.modelSnapshotId)
        }
        val attributeIds = EntityAttributeTable.select(EntityAttributeTable.id).where {
            (EntityAttributeTable.lineageId eq cmd.attributeId) and
                    (EntityAttributeTable.entitySnapshotId inSubQuery entityIds)
        }
        EntityAttributeTable.deleteWhere {
            EntityAttributeTable.id inSubQuery attributeIds
        }
    }


    // Relationship
    // ------------------------------------------------------------------------


    private fun createRelationship(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateRelationship) {
        val record = RelationshipRecord(
            snapshotId = RelationshipSnapshotId.generate(),
            lineageId = cmd.relationshipId,
            modelSnapshotId = ctx.modelSnapshotId,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description
        )
        val roles = cmd.roles.map { role ->
            RelationshipRoleRecord(
                snapshotId = RelationshipRoleSnapshotId.generate(),
                lineageId = role.id,
                relationshipSnapshotId = record.snapshotId,
                key = role.key,
                name = role.name,
                entitySnapshotId = snapshots.currentHeadEntitySnapshotIdInModelSnapshot(
                    ctx.modelSnapshotId,
                    role.entityId
                ),
                cardinality = role.cardinality.code
            )
        }
        insertRelationship(record, roles)
        searchWrite.upsertRelationshipSearchItem(ctx.modelSnapshotId, cmd.relationshipId)
    }

    private fun updateRelationshipKey(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateRelationshipKey) {
        RelationshipTable.update(where = {
            (RelationshipTable.lineageId eq cmd.relationshipId) and
                    (RelationshipTable.modelSnapshotId eq ctx.modelSnapshotId)
        }) { row ->
            row[RelationshipTable.key] = cmd.key
        }
        searchWrite.upsertRelationshipSearchItem(ctx.modelSnapshotId, cmd.relationshipId)
    }

    private fun updateRelationshipName(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateRelationshipName) {
        RelationshipTable.update(where = {
            (RelationshipTable.lineageId eq cmd.relationshipId) and
                    (RelationshipTable.modelSnapshotId eq ctx.modelSnapshotId)
        }) { row ->
            row[RelationshipTable.name] = cmd.name
        }
        searchWrite.upsertRelationshipSearchItem(ctx.modelSnapshotId, cmd.relationshipId)
    }

    private fun updateRelationshipDescription(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipDescription
    ) {
        RelationshipTable.update(where = {
            (RelationshipTable.lineageId eq cmd.relationshipId) and
                    (RelationshipTable.modelSnapshotId eq ctx.modelSnapshotId)
        }) { row ->
            row[RelationshipTable.description] = cmd.description
        }
        searchWrite.upsertRelationshipSearchItem(ctx.modelSnapshotId, cmd.relationshipId)
    }

    private fun createRelationshipRole(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateRelationshipRole) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotIdInModelSnapshot(
            ctx.modelSnapshotId,
            cmd.relationshipId
        )
        RelationshipRoleTable.insert { row ->
            row[RelationshipRoleTable.id] = RelationshipRoleSnapshotId.generate()
            row[RelationshipRoleTable.lineageId] = cmd.relationshipRoleId
            row[RelationshipRoleTable.relationshipSnapshotId] = relationshipSnapshotId
            row[RelationshipRoleTable.key] = cmd.key
            row[RelationshipRoleTable.entitySnapshotId] =
                snapshots.currentHeadEntitySnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.entityId)
            row[RelationshipRoleTable.name] = cmd.name
            row[RelationshipRoleTable.cardinality] = cmd.cardinality.code
        }
    }

    private fun updateRelationshipRole(
        ctx: ProjectionEventCtx,
        relationshipId: RelationshipId,
        relationshipRoleId: RelationshipRoleId,
        block: (UpdateStatement) -> Unit
    ) {
        val relationshipIds = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq ctx.modelSnapshotId)
        }
        RelationshipRoleTable.update(where = {
            (RelationshipRoleTable.lineageId eq relationshipRoleId) and
                    (RelationshipRoleTable.relationshipSnapshotId inSubQuery relationshipIds)
        }) { row ->
            block(row)
        }
    }


    private fun updateRelationshipRoleKey(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateRelationshipRoleKey) {
        updateRelationshipRole(ctx, cmd.relationshipId, cmd.relationshipRoleId) { row ->
            row[RelationshipRoleTable.key] = cmd.key
        }
    }

    private fun updateRelationshipRoleName(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateRelationshipRoleName) {
        updateRelationshipRole(ctx, cmd.relationshipId, cmd.relationshipRoleId) { row ->
            row[RelationshipRoleTable.name] = cmd.name
        }
    }

    private fun updateRelationshipRoleEntity(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipRoleEntity
    ) {
        updateRelationshipRole(ctx, cmd.relationshipId, cmd.relationshipRoleId) { row ->
            row[RelationshipRoleTable.entitySnapshotId] =
                snapshots.currentHeadEntitySnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.entityId)
        }
    }

    private fun updateRelationshipRoleCardinality(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipRoleCardinality
    ) {
        updateRelationshipRole(ctx, cmd.relationshipId, cmd.relationshipRoleId) { row ->
            row[RelationshipRoleTable.cardinality] = cmd.cardinality.code
        }
    }

    private fun deleteRelationshipRole(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.DeleteRelationshipRole) {
        val relationshipIds = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq cmd.relationshipId) and
                    (RelationshipTable.modelSnapshotId eq ctx.modelSnapshotId)
        }
        RelationshipRoleTable.deleteWhere {
            (RelationshipRoleTable.lineageId eq cmd.relationshipRoleId) and
                    (RelationshipRoleTable.relationshipSnapshotId inSubQuery relationshipIds)
        }
    }

    private fun addRelationshipTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateRelationshipTagAdd) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotIdInModelSnapshot(
            ctx.modelSnapshotId,
            cmd.relationshipId
        )
        val exists = RelationshipTagTable.select(RelationshipTagTable.relationshipSnapshotId).where {
            (RelationshipTagTable.relationshipSnapshotId eq relationshipSnapshotId) and
                    (RelationshipTagTable.tagId eq cmd.tagId)
        }.limit(1).any()
        if (!exists) {
            insertRelationshipTag(relationshipSnapshotId, cmd.tagId)
        }
        searchWrite.upsertRelationshipSearchItem(ctx.modelSnapshotId, cmd.relationshipId)
    }

    private fun insertRelationshipTag(
        relationshipId: RelationshipSnapshotId,
        tagId: TagId
    ) {
        RelationshipTagTable.insert { row ->
            row[RelationshipTagTable.relationshipSnapshotId] = relationshipId
            row[RelationshipTagTable.tagId] = tagId
        }
    }

    private fun insertRelationship(record: RelationshipRecord, roles: List<RelationshipRoleRecord>) {

        RelationshipTable.insert { row ->
            row[RelationshipTable.id] = record.snapshotId
            row[RelationshipTable.lineageId] = record.lineageId
            row[RelationshipTable.modelSnapshotId] = record.modelSnapshotId
            row[RelationshipTable.key] = record.key
            row[RelationshipTable.name] = record.name
            row[RelationshipTable.description] = record.description
        }
        for (roleRecord in roles) {
            RelationshipRoleTable.insert { row ->
                row[RelationshipRoleTable.id] = roleRecord.snapshotId
                row[RelationshipRoleTable.lineageId] = roleRecord.lineageId
                row[RelationshipRoleTable.relationshipSnapshotId] = roleRecord.relationshipSnapshotId
                row[RelationshipRoleTable.key] = roleRecord.key
                row[RelationshipRoleTable.entitySnapshotId] = roleRecord.entitySnapshotId
                row[RelationshipRoleTable.name] = roleRecord.name
                row[RelationshipRoleTable.cardinality] = roleRecord.cardinality
            }
        }


    }

    private fun deleteRelationship(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.DeleteRelationship) {
        searchWrite.deleteRelationshipBranch(cmd.relationshipId)
        RelationshipTable.deleteWhere {
            (RelationshipTable.lineageId eq cmd.relationshipId) and (RelationshipTable.modelSnapshotId eq ctx.modelSnapshotId)
        }
    }

    private fun deleteRelationshipTag(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.UpdateRelationshipTagDelete) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotIdInModelSnapshot(
            ctx.modelSnapshotId,
            cmd.relationshipId
        )
        RelationshipTagTable.deleteWhere {
            (RelationshipTagTable.relationshipSnapshotId eq relationshipSnapshotId) and
                    (RelationshipTagTable.tagId eq cmd.tagId)
        }
        searchWrite.upsertRelationshipSearchItem(ctx.modelSnapshotId, cmd.relationshipId)
    }
    // Relationship attribute
    // ------------------------------------------------------------------------

    private fun createRelationshipAttribute(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.CreateRelationshipAttribute) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotIdInModelSnapshot(
            ctx.modelSnapshotId,
            cmd.relationshipId
        )
        val record = RelationshipAttributeRecord(
            snapshotId = AttributeSnapshotId.generate(),
            lineageId = cmd.attributeId,
            relationshipSnapshotId = relationshipSnapshotId,
            name = cmd.name,
            key = cmd.key,
            description = cmd.description,
            typeSnapshotId = snapshots.currentHeadTypeSnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.typeId),
            optional = cmd.optional
        )
        insertRelationshipAttribute(record)
        searchWrite.upsertRelationshipAttributeSearchItem(ctx.modelSnapshotId, cmd.relationshipId, record.lineageId)
    }

    private fun updateRelationshipAttribute(
        ctx: ProjectionEventCtx,
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        block: (UpdateStatement) -> Unit
    ) {
        val relationshipIds = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq relationshipId) and
                    (RelationshipTable.modelSnapshotId eq ctx.modelSnapshotId)
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

    private fun updateRelationshipAttributeKey(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeKey
    ) {
        updateRelationshipAttribute(ctx, cmd.relationshipId, cmd.attributeId) { row ->
            row[RelationshipAttributeTable.key] = cmd.key
        }
        searchWrite.upsertRelationshipAttributeSearchItem(ctx.modelSnapshotId, cmd.relationshipId, cmd.attributeId)
    }

    private fun updateRelationshipAttributeName(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeName
    ) {
        updateRelationshipAttribute(ctx, cmd.relationshipId, cmd.attributeId) { row ->
            row[RelationshipAttributeTable.name] = cmd.name
        }
        searchWrite.upsertRelationshipAttributeSearchItem(ctx.modelSnapshotId, cmd.relationshipId, cmd.attributeId)
    }

    private fun updateRelationshipAttributeDescription(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeDescription
    ) {
        updateRelationshipAttribute(ctx, cmd.relationshipId, cmd.attributeId) { row ->
            row[RelationshipAttributeTable.description] = cmd.description
        }
        searchWrite.upsertRelationshipAttributeSearchItem(ctx.modelSnapshotId, cmd.relationshipId, cmd.attributeId)
    }

    private fun updateRelationshipAttributeType(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeType
    ) {
        updateRelationshipAttribute(ctx, cmd.relationshipId, cmd.attributeId) { row ->
            row[RelationshipAttributeTable.typeSnapshotId] =
                snapshots.currentHeadTypeSnapshotIdInModelSnapshot(ctx.modelSnapshotId, cmd.typeId)
        }
    }

    private fun updateRelationshipAttributeOptional(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeOptional
    ) {
        updateRelationshipAttribute(ctx, cmd.relationshipId, cmd.attributeId) { row ->
            row[RelationshipAttributeTable.optional] = cmd.optional
        }
    }

    private fun addRelationshipAttributeTag(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeTagAdd
    ) {
        val relationshipIds = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq cmd.relationshipId) and
                    (RelationshipTable.modelSnapshotId eq ctx.modelSnapshotId)
        }
        val attributeSnapshotId = RelationshipAttributeTable.select(RelationshipAttributeTable.id).where {
            (RelationshipAttributeTable.lineageId eq cmd.attributeId) and
                    (RelationshipAttributeTable.relationshipSnapshotId inSubQuery relationshipIds)
        }
            .single()[RelationshipAttributeTable.id]
        val exists = RelationshipAttributeTagTable.select(RelationshipAttributeTagTable.attributeSnapshotId).where {
            (RelationshipAttributeTagTable.attributeSnapshotId eq attributeSnapshotId) and (RelationshipAttributeTagTable.tagId eq cmd.tagId)
        }.limit(1).any()
        if (!exists) {
            insertRelationshipAttributeTag(attributeSnapshotId, cmd.tagId)
        }
        searchWrite.upsertRelationshipAttributeSearchItem(ctx.modelSnapshotId, cmd.relationshipId, cmd.attributeId)
    }

    private fun insertRelationshipAttributeTag(attributeId: AttributeSnapshotId, tagId: TagId) {
        RelationshipAttributeTagTable.insert { row ->
            row[RelationshipAttributeTagTable.attributeSnapshotId] = attributeId
            row[RelationshipAttributeTagTable.tagId] = tagId
        }
    }

    private fun insertRelationshipAttribute(
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

    private fun deleteRelationshipAttribute(ctx: ProjectionEventCtx, cmd: ModelStorageCmd.DeleteRelationshipAttribute) {
        searchWrite.deleteRelationshipAttributeSearchItem(cmd.attributeId)
        val relationshipIds = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq cmd.relationshipId) and
                    (RelationshipTable.modelSnapshotId eq ctx.modelSnapshotId)
        }
        val attributeIds = RelationshipAttributeTable.select(RelationshipAttributeTable.id).where {
            (RelationshipAttributeTable.lineageId eq cmd.attributeId) and
                    (RelationshipAttributeTable.relationshipSnapshotId inSubQuery relationshipIds)
        }
        RelationshipAttributeTable.deleteWhere {
            RelationshipAttributeTable.id inSubQuery attributeIds
        }
    }

    private fun deleteRelationshipAttributeTag(
        ctx: ProjectionEventCtx,
        cmd: ModelStorageCmd.UpdateRelationshipAttributeTagDelete
    ) {
        val relationshipIds = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq cmd.relationshipId) and
                    (RelationshipTable.modelSnapshotId eq ctx.modelSnapshotId)
        }
        val attributeIds = RelationshipAttributeTable.select(RelationshipAttributeTable.id).where {
            (RelationshipAttributeTable.lineageId eq cmd.attributeId) and
                    (RelationshipAttributeTable.relationshipSnapshotId inSubQuery relationshipIds)
        }
        RelationshipAttributeTagTable.deleteWhere {
            (RelationshipAttributeTagTable.attributeSnapshotId inSubQuery attributeIds) and
                    (RelationshipAttributeTagTable.tagId eq cmd.tagId)
        }
        searchWrite.upsertRelationshipAttributeSearchItem(ctx.modelSnapshotId, cmd.relationshipId, cmd.attributeId)
    }

}
