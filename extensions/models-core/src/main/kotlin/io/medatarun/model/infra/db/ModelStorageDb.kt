package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.domain.search.SearchResults
import io.medatarun.model.infra.*
import io.medatarun.model.infra.db.ModelStorageAdapters.toEntity
import io.medatarun.model.infra.db.ModelStorageAdapters.toEntityAttribute
import io.medatarun.model.infra.db.ModelStorageAdapters.toModel
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationship
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationshipAttribute
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationshipRole
import io.medatarun.model.infra.db.ModelStorageAdapters.toType
import io.medatarun.model.infra.db.events.ModelEventStreamNumberContext
import io.medatarun.model.infra.db.events.ModelEventSystem
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.tables.*
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.ports.needs.*
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ModelStorageDb(
    private val db: DbConnectionFactory,
    private val clock: ModelClock
) : ModelStorage {

    private val searchRead = ModelStorageDbSearchRead(db)
    private val searchWrite = ModelStorageDbSearchWrite(db)
    private val eventSystem = ModelEventSystem()
    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    // Model

    override fun existsModelById(id: ModelId): Boolean {
        return db.withExposed {
            ModelTable.select(ModelTable.id).where { ModelTable.id eq id }.limit(1).any()
        }
    }

    override fun existsModelByKey(key: ModelKey): Boolean {
        return db.withExposed {
            ModelSnapshotTable.select(ModelSnapshotTable.id).where {
                (ModelSnapshotTable.snapshotKind eq CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.key eq key)
            }.limit(1).any()
        }
    }

    override fun findAllModelIds(): List<ModelId> {
        return db.withExposed {
            ModelTable.selectAll().map { it[ModelTable.id] }
        }
    }

    override fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate? {
        return db.withExposed {
            val row = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.snapshotKind eq CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.key eq key)
            }.singleOrNull()
            if (row == null) null else loadModelAggregate(row)
        }
    }

    override fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate? {
        return db.withExposed {
            val row = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.snapshotKind eq CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.modelId eq id)
            }.singleOrNull()
            if (row == null) null else loadModelAggregate(row)
        }
    }

    fun findAllModelEvents(modelId: ModelId): List<ModelEventRecord> {
        return db.withExposed {
            ModelEventTable.selectAll()
                .where { ModelEventTable.modelId eq modelId }
                .orderBy(ModelEventTable.streamRevision to SortOrder.ASC)
                .map(ModelEventRecord::read)
        }
    }

    override fun findModelByKeyOptional(key: ModelKey): Model? {
        return db.withExposed {
            val row = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.snapshotKind eq CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.key eq key)
            }.singleOrNull()
            if (row == null) null else toModel(ModelRecord.read(row))
        }
    }

    override fun findModelByIdOptional(id: ModelId): Model? {
        return db.withExposed {
            val row = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.snapshotKind eq CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.modelId eq id)
            }.singleOrNull()
            if (row == null) null else toModel(ModelRecord.read(row))
        }
    }

    override fun findLatestModelReleaseVersionOptional(modelId: ModelId): ModelVersion? {
        return db.withExposed {
            ModelEventTable.select(ModelEventTable.modelVersion)
                .where {
                    (ModelEventTable.modelId eq modelId) and
                            (ModelEventTable.eventType eq modelReleaseEventType())
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
    }

    override fun findTypeByKeyOptional(
        modelId: ModelId, key: TypeKey
    ): ModelType? {
        return db.withExposed {
            val modelSnapshotId = currentHeadModelSnapshotId(modelId)
            ModelTypeTable.selectAll().where {
                (ModelTypeTable.modelSnapshotId eq modelSnapshotId) and (ModelTypeTable.key eq key)
            }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
        }
    }

    override fun findTypeByIdOptional(
        modelId: ModelId, typeId: TypeId
    ): ModelType? {
        return db.withExposed {
            val modelSnapshotId = currentHeadModelSnapshotId(modelId)
            ModelTypeTable.selectAll().where {
                (ModelTypeTable.modelSnapshotId eq modelSnapshotId) and (ModelTypeTable.lineageId eq typeId)
            }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
        }
    }

    override fun findEntityByIdOptional(
        modelId: ModelId, entityId: EntityId
    ): Entity? {
        return db.withExposed {
            val modelSnapshotId = currentHeadModelSnapshotId(modelId)
            val identifierAttributeTable = EntityAttributeTable.alias("identifier_attribute_snapshot")
            EntityTable.join(
                identifierAttributeTable,
                JoinType.INNER,
                onColumn = EntityTable.identifierAttributeSnapshotId,
                otherColumn = identifierAttributeTable[EntityAttributeTable.id]
            ).selectAll().where {
                (EntityTable.modelSnapshotId eq modelSnapshotId) and (EntityTable.lineageId eq entityId)
            }.singleOrNull()?.let { row ->
                val record = EntityRecord.read(row)
                toEntity(
                    record,
                    loadEntityTags(record.snapshotId),
                    row[identifierAttributeTable[EntityAttributeTable.lineageId]]
                )
            }
        }
    }

    override fun findEntityByKeyOptional(
        modelId: ModelId, entityKey: EntityKey
    ): Entity? {
        return db.withExposed {
            val modelSnapshotId = currentHeadModelSnapshotId(modelId)
            val identifierAttributeTable = EntityAttributeTable.alias("identifier_attribute_snapshot")
            EntityTable.join(
                identifierAttributeTable,
                JoinType.INNER,
                onColumn = EntityTable.identifierAttributeSnapshotId,
                otherColumn = identifierAttributeTable[EntityAttributeTable.id]
            ).selectAll().where {
                (EntityTable.modelSnapshotId eq modelSnapshotId) and (EntityTable.key eq entityKey)
            }.singleOrNull()?.let { row ->
                val record = EntityRecord.read(row)
                val tags = loadEntityTags(record.snapshotId)
                toEntity(record, tags, row[identifierAttributeTable[EntityAttributeTable.lineageId]])
            }
        }
    }

    override fun findEntityAttributeByIdOptional(
        modelId: ModelId, entityId: EntityId, attributeId: AttributeId
    ): Attribute? {
        return db.withExposed {
            val modelSnapshotId = currentHeadModelSnapshotId(modelId)
            val typeTable = ModelTypeTable.alias("entity_attribute_type_snapshot")
            EntityAttributeTable.join(
                EntityTable, JoinType.INNER, EntityAttributeTable.entitySnapshotId, EntityTable.id
            ).join(
                typeTable,
                JoinType.INNER,
                onColumn = EntityAttributeTable.typeSnapshotId,
                otherColumn = typeTable[ModelTypeTable.id]
            ).selectAll()
                .where { (EntityTable.modelSnapshotId eq modelSnapshotId) and (EntityTable.lineageId eq entityId) and (EntityAttributeTable.lineageId eq attributeId) }
                .singleOrNull()?.let { row ->
                    val record = EntityAttributeRecord.read(row)
                    val tags = loadEntityAttributeTags(record.snapshotId)
                    toEntityAttribute(
                        record,
                        tags,
                        row[typeTable[ModelTypeTable.lineageId]],
                        row[EntityTable.lineageId]
                    )
                }
        }
    }

    override fun findEntityAttributeByKeyOptional(
        modelId: ModelId, entityId: EntityId, key: AttributeKey
    ): Attribute? {
        return db.withExposed {
            val modelSnapshotId = currentHeadModelSnapshotId(modelId)
            val typeTable = ModelTypeTable.alias("entity_attribute_type_snapshot")
            EntityAttributeTable.join(
                EntityTable, JoinType.INNER, EntityAttributeTable.entitySnapshotId, EntityTable.id
            ).join(
                typeTable,
                JoinType.INNER,
                onColumn = EntityAttributeTable.typeSnapshotId,
                otherColumn = typeTable[ModelTypeTable.id]
            ).selectAll()
                .where { (EntityTable.modelSnapshotId eq modelSnapshotId) and (EntityTable.lineageId eq entityId) and (EntityAttributeTable.key eq key) }
                .singleOrNull()?.let { row ->
                    val record = EntityAttributeRecord.read(row)
                    val tags = loadEntityAttributeTags(record.snapshotId)
                    toEntityAttribute(
                        record,
                        tags,
                        row[typeTable[ModelTypeTable.lineageId]],
                        row[EntityTable.lineageId]
                    )
                }
        }
    }

    override fun findRelationshipByIdOptional(modelId: ModelId, relationshipId: RelationshipId): Relationship? {
        return findRelationshipByOptional(modelId, RelationshipTable.lineageId eq relationshipId)
    }

    override fun findRelationshipByKeyOptional(modelId: ModelId, relationshipKey: RelationshipKey): Relationship? {
        return findRelationshipByOptional(modelId, RelationshipTable.key eq relationshipKey)
    }


    private fun findRelationshipByOptional(modelId: ModelId, criterion: Expression<Boolean>): Relationship? {
        return db.withExposed {
            val modelSnapshotId = currentHeadModelSnapshotId(modelId)
            val roleEntityTable = EntityTable.alias("relationship_role_entity_snapshot")
            val roles = RelationshipRoleTable.join(
                RelationshipTable,
                JoinType.INNER,
                onColumn = RelationshipRoleTable.relationshipSnapshotId,
                otherColumn = RelationshipTable.id
            ).join(
                roleEntityTable,
                JoinType.INNER,
                onColumn = RelationshipRoleTable.entitySnapshotId,
                otherColumn = roleEntityTable[EntityTable.id]
            ).selectAll().where { (RelationshipTable.modelSnapshotId eq modelSnapshotId) and criterion }
                .map { row ->
                    toRelationshipRole(
                        RelationshipRoleRecord.read(row),
                        row[roleEntityTable[EntityTable.lineageId]]
                    )
                }

            RelationshipTable.selectAll().where { (RelationshipTable.modelSnapshotId eq modelSnapshotId) and criterion }
                .singleOrNull()
                ?.let { row ->
                    val record = RelationshipRecord.read(row)
                    val tags = loadRelationshipTags(record.snapshotId)
                    toRelationship(record, roles, tags)
                }
        }
    }

    override fun findRelationshipRoleByIdOptional(
        modelId: ModelId, relationshipId: RelationshipId, roleId: RelationshipRoleId
    ): RelationshipRole? {
        return findRelationshipRoleByOptional(modelId, relationshipId, RelationshipRoleTable.lineageId eq roleId)
    }

    override fun findRelationshipRoleByKeyOptional(
        modelId: ModelId, relationshipId: RelationshipId, roleKey: RelationshipRoleKey
    ): RelationshipRole? {
        return findRelationshipRoleByOptional(modelId, relationshipId, RelationshipRoleTable.key eq roleKey)
    }

    private fun findRelationshipRoleByOptional(
        modelId: ModelId, relationshipId: RelationshipId, criterion: Op<Boolean>
    ): RelationshipRole? {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
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


    override fun findRelationshipAttributeByIdOptional(
        modelId: ModelId, relationshipId: RelationshipId, attributeId: AttributeId
    ): Attribute? {
        return findRelationshipAttributeByOptional(
            modelId, relationshipId, RelationshipAttributeTable.lineageId eq attributeId
        )
    }

    override fun findRelationshipAttributeByKeyOptional(
        modelId: ModelId, relationshipId: RelationshipId, key: AttributeKey
    ): Attribute? {
        return findRelationshipAttributeByOptional(
            modelId, relationshipId, RelationshipAttributeTable.key eq key
        )
    }


    fun findRelationshipAttributeByOptional(
        modelId: ModelId, relationshipId: RelationshipId, criterion: Expression<Boolean>
    ): Attribute? {
        return db.withExposed {
            val modelSnapshotId = currentHeadModelSnapshotId(modelId)
            val typeTable = ModelTypeTable.alias("relationship_attribute_type_snapshot")
            RelationshipAttributeTable.join(
                RelationshipTable,
                JoinType.INNER,
                RelationshipAttributeTable.relationshipSnapshotId,
                RelationshipTable.id
            ).join(
                typeTable,
                JoinType.INNER,
                onColumn = RelationshipAttributeTable.typeSnapshotId,
                otherColumn = typeTable[ModelTypeTable.id]
            ).selectAll()
                .where { (RelationshipTable.modelSnapshotId eq modelSnapshotId) and (RelationshipTable.lineageId eq relationshipId) and criterion }
                .singleOrNull()?.let { row ->
                    val record = RelationshipAttributeRecord.read(row)
                    val tags = loadRelationshipAttributeTags(record.snapshotId)
                    toRelationshipAttribute(
                        record,
                        tags,
                        row[typeTable[ModelTypeTable.lineageId]],
                        row[RelationshipTable.lineageId]
                    )
                }
        }
    }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    override fun search(query: ModelStorageSearchQuery): SearchResults {
        return searchRead.search(query)
    }

    // -------------------------------------------------------------------------
    // Analytics
    // -------------------------------------------------------------------------

    override fun isTypeUsedInEntityAttributes(
        modelId: ModelId, typeId: TypeId
    ): Boolean {
        return db.withExposed {
            val modelSnapshotId = currentHeadModelSnapshotId(modelId)
            val typeSnapshotId = currentHeadTypeSnapshotId(modelId, typeId)
            EntityAttributeTable.join(
                EntityTable,
                JoinType.INNER,
                onColumn = EntityAttributeTable.entitySnapshotId,
                otherColumn = EntityTable.id
            ).selectAll().where {
                (EntityAttributeTable.typeSnapshotId eq typeSnapshotId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }.any()
        }
    }

    override fun isTypeUsedInRelationshipAttributes(
        modelId: ModelId, typeId: TypeId
    ): Boolean {
        return db.withExposed {
            val modelSnapshotId = currentHeadModelSnapshotId(modelId)
            val typeSnapshotId = currentHeadTypeSnapshotId(modelId, typeId)
            RelationshipAttributeTable.join(
                RelationshipTable,
                JoinType.INNER,
                onColumn = RelationshipAttributeTable.relationshipSnapshotId,
                otherColumn = RelationshipTable.id
            ).selectAll().where {
                (RelationshipAttributeTable.typeSnapshotId eq typeSnapshotId) and (RelationshipTable.modelSnapshotId eq modelSnapshotId)
            }.any()
        }

    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    override fun dispatch(cmdEnv: ModelStorageCmdEnveloppe) {
        db.withExposed {
            val modelId = extractModelId(cmdEnv.cmd)
            val streamNumberCtx = eventSystem.eventStreamNumberManager.createNumberContext(modelId)
            prepareStorageForAppend(cmdEnv.cmd)
            dispatchExposed(cmdEnv, streamNumberCtx)
        }
    }

    private fun dispatchExposed(
        cmdEnv: ModelStorageCmdEnveloppe,
        streamNumberCtx: ModelEventStreamNumberContext
    ) {
        val cmd = cmdEnv.cmd
        if (cmd is ModelStorageCmd.DeleteModel) {
            appendModelEvent(cmdEnv, streamNumberCtx)
            deleteModel(cmd.modelId)
            return
        }
        val record = appendModelEvent(cmdEnv, streamNumberCtx)
        projectModelEvent(record)
        updateCurrentHeadProjectionMetadata(extractModelId(cmd), record.streamRevision)
    }

    private fun projectCommand(cmd: ModelStorageCmd) {
        when (cmd) {
            //@formatter:off
            is ModelStorageCmd.StoreModelAggregate -> storeModelAggregate(cmd)
            is ModelStorageCmd.CreateModel -> createModel(cmd)
            is ModelStorageCmd.UpdateModelName -> updateModelName(cmd.modelId, cmd.name)
            is ModelStorageCmd.UpdateModelKey -> updateModelKey(cmd.modelId, cmd.key)
            is ModelStorageCmd.UpdateModelDescription -> updateModelDescription(cmd.modelId, cmd.description)
            is ModelStorageCmd.UpdateModelAuthority -> updateModelAuthority(cmd.modelId, cmd.authority)
            is ModelStorageCmd.ModelRelease -> releaseModel(cmd.modelId, cmd.version)
            is ModelStorageCmd.UpdateModelDocumentationHome -> updateModelDocumentationHome(cmd.modelId, cmd.url)
            is ModelStorageCmd.UpdateModelTagAdd -> addModelTag(cmd.modelId, cmd.tagId)
            is ModelStorageCmd.UpdateModelTagDelete -> deleteModelTag(cmd.modelId, cmd.tagId)
            is ModelStorageCmd.CreateType -> createType(cmd)
            is ModelStorageCmd.UpdateTypeKey -> updateTypeKey(cmd.modelId, cmd.typeId, cmd.value)
            is ModelStorageCmd.UpdateTypeName -> updateTypeName(cmd.modelId, cmd.typeId, cmd.value)
            is ModelStorageCmd.UpdateTypeDescription -> updateTypeDescription(cmd.modelId, cmd.typeId, cmd.value)
            is ModelStorageCmd.DeleteType -> deleteType(cmd.modelId, cmd.typeId)
            is ModelStorageCmd.CreateEntity -> createEntity(cmd)
            is ModelStorageCmd.UpdateEntityKey -> updateEntityKey(cmd.modelId, cmd.entityId, cmd.value)
            is ModelStorageCmd.UpdateEntityName -> updateEntityName(cmd.modelId, cmd.entityId, cmd.value)
            is ModelStorageCmd.UpdateEntityDescription -> updateEntityDescription(cmd.modelId, cmd.entityId, cmd.value)
            is ModelStorageCmd.UpdateEntityIdentifierAttribute -> updateEntityIdentifierAttribute(cmd.modelId, cmd.entityId, cmd.value)
            is ModelStorageCmd.UpdateEntityDocumentationHome -> updateEntityDocumentationHome(cmd.modelId, cmd.entityId, cmd.value)
            is ModelStorageCmd.UpdateEntityTagAdd -> addEntityTag(cmd.modelId, cmd.entityId, cmd.tagId)
            is ModelStorageCmd.UpdateEntityTagDelete -> deleteEntityTag(cmd.modelId, cmd.entityId, cmd.tagId)
            is ModelStorageCmd.DeleteEntity -> deleteEntity(cmd.modelId, cmd.entityId)
            is ModelStorageCmd.CreateEntityAttribute -> createEntityAttribute(cmd)
            is ModelStorageCmd.UpdateEntityAttributeKey -> updateEntityAttributeKey(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateEntityAttributeName -> updateEntityAttributeName(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateEntityAttributeDescription -> updateEntityAttributeDescription(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateEntityAttributeType -> updateEntityAttributeType(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateEntityAttributeOptional -> updateEntityAttributeOptional(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateEntityAttributeTagAdd -> addEntityAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelStorageCmd.UpdateEntityAttributeTagDelete -> deleteEntityAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelStorageCmd.DeleteEntityAttribute -> deleteEntityAttribute(cmd.entityId, cmd.attributeId)
            is ModelStorageCmd.CreateRelationship -> createRelationship(cmd)
            is ModelStorageCmd.UpdateRelationshipKey -> updateRelationshipKey(cmd.relationshipId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipName -> updateRelationshipName(cmd.relationshipId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipDescription -> updateRelationshipDescription(cmd.relationshipId, cmd.value)
            is ModelStorageCmd.CreateRelationshipRole -> createRelationshipRole(cmd)
            is ModelStorageCmd.UpdateRelationshipRoleKey -> updateRelationshipRoleKey(cmd.relationshipRoleId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipRoleName -> updateRelationshipRoleName(cmd.relationshipRoleId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipRoleEntity -> updateRelationshipRoleEntity(cmd.relationshipRoleId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipRoleCardinality -> updateRelationshipRoleCardinality(cmd.relationshipRoleId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipTagAdd -> addRelationshipTag(cmd.relationshipId, cmd.tagId)
            is ModelStorageCmd.UpdateRelationshipTagDelete -> deleteRelationshipTag(cmd.relationshipId, cmd.tagId)
            is ModelStorageCmd.DeleteRelationship -> deleteRelationship(cmd.modelId, cmd.relationshipId)
            is ModelStorageCmd.DeleteRelationshipRole -> deleteRelationshipRole(cmd.relationshipId, cmd.relationshipRoleId)
            is ModelStorageCmd.CreateRelationshipAttribute -> createRelationshipAttribute(cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeKey -> updateRelationshipAttributeKey(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipAttributeName -> updateRelationshipAttributeName(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipAttributeDescription -> updateRelationshipAttributeDescription(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipAttributeType -> updateRelationshipAttributeType(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipAttributeOptional -> updateRelationshipAttributeOptional(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipAttributeTagAdd -> addRelationshipAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelStorageCmd.UpdateRelationshipAttributeTagDelete -> deleteRelationshipAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelStorageCmd.DeleteRelationshipAttribute -> deleteRelationshipAttribute(cmd.modelId, cmd.relationshipId, cmd.attributeId)
            is ModelStorageCmd.DeleteModel -> throw ModelStorageDbUnsupportedProjectedDeleteException("model_deleted")
            //@formatter:on
        }
    }

    private fun appendModelEvent(
        cmdEnv: ModelStorageCmdEnveloppe,
        streamNumberCtx: ModelEventStreamNumberContext
    ): ModelEventRecord {
        val record = eventSystem.recordFactory.create(
            cmdEnv = cmdEnv,
            streamRevision = streamNumberCtx.nextRevision(),
            createdAt = clock.now()
        )
        try {
            ModelEventTable.insert { row ->
                row[ModelEventTable.id] = record.id
                row[ModelEventTable.modelId] = record.modelId
                row[ModelEventTable.streamRevision] = record.streamRevision
                row[ModelEventTable.eventType] = record.eventType
                row[ModelEventTable.eventVersion] = record.eventVersion
                row[ModelEventTable.modelVersion] = record.modelVersion
                row[ModelEventTable.actorId] = record.actorId
                row[ModelEventTable.actionId] = record.actionId
                row[ModelEventTable.createdAt] = record.createdAt.toString()
                row[ModelEventTable.payload] = record.payload
            }
        } catch (e: Exception) {
            eventSystem.eventStreamNumberManager.rethrowIfStreamRevisionConflict(
                exception = e,
                numberContext = streamNumberCtx,
                conflictingRevision = record.streamRevision
            )
            throw e
        }
        eventSystem.eventStreamNumberManager.onAppendCommitted(streamNumberCtx, record.streamRevision)
        return record
    }

    private fun projectModelEvent(record: ModelEventRecord) {
        val cmd = eventSystem.codec.decode(
            io.medatarun.model.infra.db.events.ModelEventEncoded(
                eventType = record.eventType,
                eventVersion = record.eventVersion,
                payload = record.payload
            )
        )
        projectCommand(cmd)
    }

    private fun prepareStorageForAppend(cmd: ModelStorageCmd) {
        when (cmd) {
            is ModelStorageCmd.CreateModel -> ensureModelIdentityExists(cmd.id)
            is ModelStorageCmd.StoreModelAggregate -> ensureModelIdentityExists(cmd.model.id)
            else -> return
        }
    }

    private fun ensureModelIdentityExists(modelId: ModelId) {
        val exists = ModelTable.select(ModelTable.id).where { ModelTable.id eq modelId }.limit(1).any()
        if (!exists) {
            ModelTable.insert { row ->
                row[ModelTable.id] = modelId
            }
        }
    }

    private fun extractModelId(cmd: ModelStorageCmd): ModelId {
        return when (cmd) {
            is ModelStorageCmd.CreateModel -> cmd.id
            is ModelStorageCmd.StoreModelAggregate -> cmd.model.id
            is ModelStorageCmdOnModel -> cmd.modelId
        }
    }

    private fun generateCurrentHeadModelSnapshotId(): ModelId {
        return ModelId.generate()
    }

    private fun findCurrentHeadModelSnapshotId(modelId: ModelId): ModelId? {
        val row = ModelSnapshotTable.select(ModelSnapshotTable.id).where {
            currentHeadModelSnapshotCriteria(modelId)
        }.singleOrNull()
        if (row == null) {
            return null
        }
        return ModelId.fromString(row[ModelSnapshotTable.id])
    }

    /**
     * Mutable model-level state lives only on the current head projection.
     * Version snapshots must stay immutable once they exist.
     */
    private fun currentHeadModelSnapshotCriteria(modelId: ModelId): Op<Boolean> {
        return (ModelSnapshotTable.modelId eq modelId) and
                (ModelSnapshotTable.snapshotKind eq CURRENT_HEAD_SNAPSHOT_KIND)
    }

    private fun currentHeadModelSnapshotId(modelId: ModelId): ModelId {
        return findCurrentHeadModelSnapshotId(modelId) ?: throw ModelStorageDbMissingCurrentHeadModelSnapshotException(
            modelId
        )
    }

    /**
     * While CURRENT_HEAD is still fed by direct writes, keep projection metadata
     * aligned with the latest event revision that has been applied.
     */
    private fun updateCurrentHeadProjectionMetadata(modelId: ModelId, upToRevision: Int) {
        val now = clock.now().toString()
        ModelSnapshotTable.update(where = { currentHeadModelSnapshotCriteria(modelId) }) { row ->
            row[ModelSnapshotTable.upToRevision] = upToRevision
            row[ModelSnapshotTable.updatedAt] = now
        }
    }

    private fun currentHeadTypeSnapshotId(modelId: ModelId, typeId: TypeId): TypeId {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        val row = ModelTypeTable.select(ModelTypeTable.id).where {
            (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
        }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingTypeSnapshotException(typeId)
        }
        return row[ModelTypeTable.id]
    }

    private fun currentHeadEntitySnapshotId(modelId: ModelId, entityId: EntityId): EntityId {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        val row = EntityTable.select(EntityTable.id).where {
            (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
        }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingEntitySnapshotException(entityId)
        }
        return row[EntityTable.id]
    }

    private fun currentHeadAttributeSnapshotId(modelId: ModelId, attributeId: AttributeId): AttributeId {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        val entityAttributeRow = EntityAttributeTable.join(
            EntityTable,
            JoinType.INNER,
            EntityAttributeTable.entitySnapshotId,
            EntityTable.id
        ).select(EntityAttributeTable.id)
            .where {
                (EntityAttributeTable.lineageId eq attributeId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }
            .singleOrNull()
        if (entityAttributeRow != null) {
            return entityAttributeRow[EntityAttributeTable.id]
        }
        val relationshipAttributeRow = RelationshipAttributeTable.join(
            RelationshipTable,
            JoinType.INNER,
            RelationshipAttributeTable.relationshipSnapshotId,
            RelationshipTable.id
        ).select(RelationshipAttributeTable.id)
            .where {
                (RelationshipAttributeTable.lineageId eq attributeId) and
                        (RelationshipTable.modelSnapshotId eq modelSnapshotId)
            }
            .singleOrNull()
        if (relationshipAttributeRow != null) {
            return relationshipAttributeRow[RelationshipAttributeTable.id]
        }
        throw ModelStorageDbMissingAttributeSnapshotException(attributeId)
    }

    private fun currentHeadRelationshipSnapshotId(modelId: ModelId, relationshipId: RelationshipId): RelationshipId {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        val row = RelationshipTable.select(RelationshipTable.id).where {
            (RelationshipTable.lineageId eq relationshipId) and (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingRelationshipSnapshotException(relationshipId)
        }
        return row[RelationshipTable.id]
    }

    private fun modelIdForEntity(entityId: EntityId): ModelId {
        val row = EntityTable.select(EntityTable.modelSnapshotId).where { EntityTable.lineageId eq entityId }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingEntitySnapshotException(entityId)
        }
        return modelIdForModelSnapshot(row[EntityTable.modelSnapshotId])
    }

    private fun modelIdForEntitySnapshot(entitySnapshotId: EntityId): ModelId {
        val row = EntityTable.select(EntityTable.modelSnapshotId).where { EntityTable.id eq entitySnapshotId }.singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingEntitySnapshotException(entitySnapshotId)
        }
        return modelIdForModelSnapshot(row[EntityTable.modelSnapshotId])
    }

    private fun modelIdForEntityAttribute(attributeId: AttributeId): ModelId {
        val row = EntityAttributeTable.join(
            EntityTable,
            JoinType.INNER,
            EntityAttributeTable.entitySnapshotId,
            EntityTable.id
        ).select(EntityTable.modelSnapshotId)
            .where { EntityAttributeTable.lineageId eq attributeId }
            .singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingAttributeSnapshotException(attributeId)
        }
        return modelIdForModelSnapshot(row[EntityTable.modelSnapshotId])
    }

    private fun modelIdForRelationship(relationshipId: RelationshipId): ModelId {
        val row = RelationshipTable.select(RelationshipTable.modelSnapshotId)
            .where { RelationshipTable.lineageId eq relationshipId }
            .singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingRelationshipSnapshotException(relationshipId)
        }
        return modelIdForModelSnapshot(row[RelationshipTable.modelSnapshotId])
    }

    private fun modelIdForRelationshipRole(relationshipRoleId: RelationshipRoleId): ModelId {
        val row = RelationshipRoleTable.join(
            RelationshipTable,
            JoinType.INNER,
            RelationshipRoleTable.relationshipSnapshotId,
            RelationshipTable.id
        ).select(RelationshipTable.modelSnapshotId)
            .where { RelationshipRoleTable.lineageId eq relationshipRoleId }
            .singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingRelationshipRoleSnapshotException(relationshipRoleId.asString())
        }
        return modelIdForModelSnapshot(row[RelationshipTable.modelSnapshotId])
    }

    private fun modelIdForRelationshipAttribute(attributeId: AttributeId): ModelId {
        val row = RelationshipAttributeTable.join(
            RelationshipTable,
            JoinType.INNER,
            RelationshipAttributeTable.relationshipSnapshotId,
            RelationshipTable.id
        ).select(RelationshipTable.modelSnapshotId)
            .where { RelationshipAttributeTable.lineageId eq attributeId }
            .singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingAttributeSnapshotException(attributeId)
        }
        return modelIdForModelSnapshot(row[RelationshipTable.modelSnapshotId])
    }

    private fun modelIdForModelSnapshot(modelSnapshotId: ModelId): ModelId {
        val row = ModelSnapshotTable.select(ModelSnapshotTable.modelId)
            .where { ModelSnapshotTable.id eq modelSnapshotId.asString() }
            .singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingCurrentHeadModelSnapshotException(modelSnapshotId)
        }
        return row[ModelSnapshotTable.modelId]
    }

    // Model
    // ------------------------------------------------------------------------

    private fun loadModelAggregate(row: ResultRow): ModelAggregateInMemory {
        val record = ModelRecord.read(row)
        val types = loadTypes(record.modelId)
        val entities = loadEntities(record.modelId)
        val entityAttributes = loadEntityAttributes(record.modelId)
        val relationships = loadRelationships(record.modelId)
        val relationshipAttributes = loadRelationshipAttributes(record.modelId)

        return ModelAggregateInMemory(
            model = toModel(record),
            types = types,
            entities = entities,
            relationships = relationships,
            tags = loadModelTags(record.snapshotId),
            attributes = entityAttributes + relationshipAttributes
        )
    }

    private fun loadModelTags(modelSnapshotId: String): List<TagId> {
        return ModelTagTable.selectAll().where { ModelTagTable.modelSnapshotId eq ModelId.fromString(modelSnapshotId) }
            .orderBy(ModelTagTable.tagId to SortOrder.ASC).map { it[ModelTagTable.tagId] }
    }

    private fun createModel(cmd: ModelStorageCmd.CreateModel) {
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
        insertModel(inMemoryModel)
        searchWrite.upsertModelSearchItem(inMemoryModel.id)
    }

    private fun storeModelAggregate(model: ModelStorageCmd.StoreModelAggregate) {

        logger.warn("Storing full aggregate {}", model)

        val modelId = model.model.id

        val modelSnapshotId = insertModel(
            ModelInMemory(
                id = model.model.id,
                key = model.model.key,
                name = model.model.name,
                description = model.model.description,
                version = model.model.version,
                origin = model.model.origin,
                authority = model.model.authority,
                documentationHome = model.model.documentationHome,
            )
        )

        for (type in model.types) {
            insertType(
                ModelTypeRecord(
                    snapshotId = type.id,
                    lineageId = type.id,
                    modelSnapshotId = modelSnapshotId,
                    key = type.key,
                    name = type.name,
                    description = type.description
                )
            )
        }

        for (entity in model.entities) {
            insertEntity(
                EntityRecord(
                    snapshotId = entity.id,
                    lineageId = entity.id,
                    modelSnapshotId = modelSnapshotId,
                    key = entity.key,
                    name = entity.name,
                    description = entity.description,
                    identifierAttributeSnapshotId = entity.identifierAttributeId,
                    origin = entity.origin,
                    documentationHome = entity.documentationHome?.toExternalForm(),
                )
            )
            searchWrite.upsertEntitySearchItem(modelId, entity.id)

            for (attr in model.entityAttributes.filter { it.entityId == entity.id }) {
                insertEntityAttribute(
                    EntityAttributeRecord(
                        snapshotId = attr.id,
                        lineageId = attr.id,
                        entitySnapshotId = entity.id,
                        key = attr.key,
                        name = attr.name,
                        description = attr.description,
                        typeSnapshotId = attr.typeId,
                        optional = attr.optional
                    )
                )
                searchWrite.upsertEntityAttributeSearchItem(modelId, attr.id)
            }
        }

        for (relationship in model.relationships) {
            insertRelationship(
                record = RelationshipRecord(
                    snapshotId = relationship.id,
                    lineageId = relationship.id,
                    modelSnapshotId = modelSnapshotId,
                    key = relationship.key,
                    name = relationship.name,
                    description = relationship.description
                ),
                roles = relationship.roles.map { role ->
                    RelationshipRoleRecord(
                        snapshotId = role.id,
                        lineageId = role.id,
                        relationshipSnapshotId = relationship.id,
                        key = role.key,
                        entitySnapshotId = role.entityId,
                        name = role.name,
                        cardinality = role.cardinality.code
                    )
                }
            )
            searchWrite.upsertRelationshipSearchItem(modelId, relationship.id)

            for (attr in model.relationshipAttributes.filter { it.relationshipId == relationship.id }) {
                insertRelationshipAttribute(
                    RelationshipAttributeRecord(
                        snapshotId = attr.id,
                        lineageId = attr.id,
                        relationshipSnapshotId = relationship.id,
                        key = attr.key,
                        name = attr.name,
                        description = attr.description,
                        typeSnapshotId = attr.typeId,
                        optional = attr.optional
                    )
                )
                searchWrite.upsertRelationshipAttributeSearchItem(modelId, attr.id)
            }
        }

        searchWrite.upsertModelSearchItem(modelId)

    }

    private fun deleteModel(modelId: ModelId) {
        searchWrite.deleteModelBranch(modelId)
        ModelTable.deleteWhere { id eq modelId }
    }

    private fun updateModelName(modelId: ModelId, name: LocalizedText) {
        ModelSnapshotTable.update(where = { currentHeadModelSnapshotCriteria(modelId) }) { row ->
            row[ModelSnapshotTable.name] = name
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun updateModelKey(modelId: ModelId, key: ModelKey) {
        ModelSnapshotTable.update(where = { currentHeadModelSnapshotCriteria(modelId) }) { row ->
            row[ModelSnapshotTable.key] = key
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun updateModelDescription(modelId: ModelId, description: LocalizedMarkdown?) {
        ModelSnapshotTable.update(where = { currentHeadModelSnapshotCriteria(modelId) }) { row ->
            row[ModelSnapshotTable.description] = description
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun updateModelAuthority(modelId: ModelId, authority: ModelAuthority) {
        ModelSnapshotTable.update(where = { currentHeadModelSnapshotCriteria(modelId) }) { row ->
            row[ModelSnapshotTable.authority] = authority
        }
    }

    private fun releaseModel(modelId: ModelId, version: ModelVersion) {
        ModelSnapshotTable.update(where = { currentHeadModelSnapshotCriteria(modelId) }) { row ->
            row[ModelSnapshotTable.version] = version.value
        }
        createVersionSnapshotFromCurrentHead(modelId, version)
    }

    private fun updateModelDocumentationHome(modelId: ModelId, documentationHome: java.net.URL?) {
        ModelSnapshotTable.update(where = { currentHeadModelSnapshotCriteria(modelId) }) { row ->
            row[ModelSnapshotTable.documentationHome] = documentationHome?.toExternalForm()
        }
    }

    private fun addModelTag(modelId: ModelId, tagId: TagId) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        val exists = ModelTagTable.select(ModelTagTable.modelSnapshotId).where {
            (ModelTagTable.modelSnapshotId eq modelSnapshotId) and (ModelTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            ModelTagTable.insert { row ->
                row[ModelTagTable.modelSnapshotId] = modelSnapshotId
                row[ModelTagTable.tagId] = tagId
            }
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun deleteModelTag(modelId: ModelId, tagId: TagId) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        ModelTagTable.deleteWhere {
            (ModelTagTable.modelSnapshotId eq modelSnapshotId) and (ModelTagTable.tagId eq tagId)
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun insertModel(model: Model): ModelId {
        val modelSnapshotId = generateCurrentHeadModelSnapshotId()
        ModelSnapshotTable.insert { row ->
            row[ModelSnapshotTable.id] = modelSnapshotId.asString()
            row[ModelSnapshotTable.modelId] = model.id
            row[ModelSnapshotTable.key] = model.key
            row[ModelSnapshotTable.name] = model.name
            row[ModelSnapshotTable.description] = model.description
            row[ModelSnapshotTable.origin] = model.origin
            row[ModelSnapshotTable.authority] = model.authority
            row[ModelSnapshotTable.documentationHome] = model.documentationHome?.toExternalForm()
            row[ModelSnapshotTable.snapshotKind] = CURRENT_HEAD_SNAPSHOT_KIND
            row[ModelSnapshotTable.upToRevision] = 0
            row[ModelSnapshotTable.modelEventReleaseId] = null
            row[ModelSnapshotTable.version] = model.version.value
            row[ModelSnapshotTable.createdAt] = clock.now().toString()
            row[ModelSnapshotTable.updatedAt] = clock.now().toString()
        }
        return modelSnapshotId
    }

    /**
     * Creates a frozen VERSION_SNAPSHOT by cloning the current head rows and
     * remapping every internal snapshot reference to fresh snapshot ids.
     */
    private fun createVersionSnapshotFromCurrentHead(modelId: ModelId, version: ModelVersion) {
        val currentHeadSnapshotId = currentHeadModelSnapshotId(modelId)
        val currentHeadRow =
            ModelSnapshotTable.selectAll().where { currentHeadModelSnapshotCriteria(modelId) }.singleOrNull()
                ?: throw ModelStorageDbMissingCurrentHeadModelSnapshotException(modelId)
        val currentHeadRecord = ModelRecord.read(currentHeadRow)
        val releaseEvent = findLatestReleaseEvent(modelId, version)
        val versionSnapshotId = ModelId.generate()
        val now = clock.now().toString()

        ModelSnapshotTable.insert { row ->
            row[ModelSnapshotTable.id] = versionSnapshotId.asString()
            row[ModelSnapshotTable.modelId] = currentHeadRecord.modelId
            row[ModelSnapshotTable.key] = currentHeadRecord.key
            row[ModelSnapshotTable.name] = currentHeadRecord.name
            row[ModelSnapshotTable.description] = currentHeadRecord.description
            row[ModelSnapshotTable.origin] = currentHeadRecord.origin
            row[ModelSnapshotTable.authority] = currentHeadRecord.authority
            row[ModelSnapshotTable.documentationHome] = currentHeadRecord.documentationHome
            row[ModelSnapshotTable.snapshotKind] = VERSION_SNAPSHOT_KIND
            row[ModelSnapshotTable.upToRevision] = releaseEvent.streamRevision
            row[ModelSnapshotTable.modelEventReleaseId] = releaseEvent.id
            row[ModelSnapshotTable.version] = version.value
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

    private fun findLatestReleaseEvent(modelId: ModelId, version: ModelVersion): ModelEventRecord {
        val row = ModelEventTable.selectAll().where {
            (ModelEventTable.modelId eq modelId) and
                    (ModelEventTable.eventType eq modelReleaseEventType()) and
                    (ModelEventTable.modelVersion eq version.value)
        }.orderBy(ModelEventTable.streamRevision to SortOrder.DESC).limit(1).singleOrNull()
        if (row == null) {
            throw ModelStorageDbMissingReleaseEventException(modelId, version.value)
        }
        return ModelEventRecord.read(row)
    }

    private fun cloneModelTags(currentHeadSnapshotId: ModelId, versionSnapshotId: ModelId) {
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

    private fun cloneTypeSnapshots(currentHeadSnapshotId: ModelId, versionSnapshotId: ModelId): Map<TypeId, TypeId> {
        val rows = ModelTypeTable.selectAll().where { ModelTypeTable.modelSnapshotId eq currentHeadSnapshotId }
        val snapshotIdMap = mutableMapOf<TypeId, TypeId>()
        for (row in rows) {
            val record = ModelTypeRecord.read(row)
            val versionTypeSnapshotId = TypeId.generate()
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
        currentHeadSnapshotId: ModelId,
        versionSnapshotId: ModelId,
        typeSnapshotIdMap: Map<TypeId, TypeId>
    ): Map<EntityId, EntityId> {
        val entityRows = EntityTable.selectAll().where { EntityTable.modelSnapshotId eq currentHeadSnapshotId }
        val currentHeadEntityRecords = entityRows.map { EntityRecord.read(it) }
        val currentHeadAttributeRecords = EntityAttributeTable.join(
            EntityTable,
            JoinType.INNER,
            onColumn = EntityAttributeTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).selectAll().where { EntityTable.modelSnapshotId eq currentHeadSnapshotId }
            .map { EntityAttributeRecord.read(it) }

        val entitySnapshotIdMap = mutableMapOf<EntityId, EntityId>()
        val attributeSnapshotIdMap = mutableMapOf<AttributeId, AttributeId>()

        for (record in currentHeadEntityRecords) {
            entitySnapshotIdMap[record.snapshotId] = EntityId.generate()
        }
        for (record in currentHeadAttributeRecords) {
            attributeSnapshotIdMap[record.snapshotId] = AttributeId.generate()
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

    private fun cloneEntityTags(entitySnapshotIdMap: Map<EntityId, EntityId>) {
        for (entry in entitySnapshotIdMap.entries) {
            val tagIds = EntityTagTable.select(EntityTagTable.tagId)
                .where { EntityTagTable.entitySnapshotId eq entry.key }
                .map { it[EntityTagTable.tagId] }
            for (tagId in tagIds) {
                insertEntityTag(entry.value, tagId)
            }
        }
    }

    private fun cloneEntityAttributeTags(attributeSnapshotIdMap: Map<AttributeId, AttributeId>) {
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
        currentHeadSnapshotId: ModelId,
        versionSnapshotId: ModelId
    ): Map<RelationshipId, RelationshipId> {
        val rows = RelationshipTable.selectAll().where { RelationshipTable.modelSnapshotId eq currentHeadSnapshotId }
        val snapshotIdMap = mutableMapOf<RelationshipId, RelationshipId>()
        for (row in rows) {
            val record = RelationshipRecord.read(row)
            val versionRelationshipSnapshotId = RelationshipId.generate()
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

    private fun cloneRelationshipTags(relationshipSnapshotIdMap: Map<RelationshipId, RelationshipId>) {
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
        relationshipSnapshotIdMap: Map<RelationshipId, RelationshipId>,
        entitySnapshotIdMap: Map<EntityId, EntityId>
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
                insertRow[RelationshipRoleTable.id] = RelationshipRoleId.generate()
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
        relationshipSnapshotIdMap: Map<RelationshipId, RelationshipId>,
        typeSnapshotIdMap: Map<TypeId, TypeId>
    ) {
        if (relationshipSnapshotIdMap.isEmpty()) {
            return
        }
        val rows = RelationshipAttributeTable.selectAll().where {
            RelationshipAttributeTable.relationshipSnapshotId inList relationshipSnapshotIdMap.keys.toList()
        }
        val attributeSnapshotIdMap = mutableMapOf<AttributeId, AttributeId>()

        for (row in rows) {
            val record = RelationshipAttributeRecord.read(row)
            val versionAttributeSnapshotId = AttributeId.generate()
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

    private fun loadTypes(modelId: ModelId): List<ModelTypeInMemory> {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        return ModelTypeTable.selectAll().where { ModelTypeTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(ModelTypeTable.key to SortOrder.ASC).map { row ->
                toType(ModelTypeRecord.read(row))
            }
    }

    private fun createType(cmd: ModelStorageCmd.CreateType) {
        val lineageId = TypeId.generate()
        val record = ModelTypeRecord(
            snapshotId = TypeId.generate(),
            lineageId = lineageId,
            modelSnapshotId = currentHeadModelSnapshotId(cmd.modelId),
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

    private fun updateTypeKey(modelId: ModelId, typeId: TypeId, value: TypeKey) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[ModelTypeTable.key] = value
        }
    }

    private fun updateTypeName(modelId: ModelId, typeId: TypeId, value: LocalizedText?) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[ModelTypeTable.name] = value
        }
    }

    private fun updateTypeDescription(modelId: ModelId, typeId: TypeId, value: LocalizedMarkdown?) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[ModelTypeTable.description] = value
        }
    }

    private fun deleteType(modelId: ModelId, typeId: TypeId) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        ModelTypeTable.deleteWhere {
            (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
        }
    }

    // Entity
    // ------------------------------------------------------------------------

    private fun loadEntities(modelId: ModelId): List<EntityInMemory> {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        val identifierAttributeTable = EntityAttributeTable.alias("identifier_attribute_snapshot")
        return EntityTable.join(
            identifierAttributeTable,
            JoinType.INNER,
            onColumn = EntityTable.identifierAttributeSnapshotId,
            otherColumn = identifierAttributeTable[EntityAttributeTable.id]
        ).selectAll().where { EntityTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(EntityTable.key to SortOrder.ASC).map { row ->
                val record = EntityRecord.read(row)
                val tags = loadEntityTags(record.snapshotId)
                toEntity(record, tags, row[identifierAttributeTable[EntityAttributeTable.lineageId]])
            }
    }

    private fun loadEntityTags(entityId: EntityId): List<TagId> {
        return EntityTagTable.selectAll().where { EntityTagTable.entitySnapshotId eq entityId }
            .orderBy(EntityTagTable.tagId to SortOrder.ASC).map { it[EntityTagTable.tagId] }
    }

    private fun createEntity(cmd: ModelStorageCmd.CreateEntity) {
        insertEntity(cmd)
        searchWrite.upsertEntitySearchItem(cmd.modelId, cmd.entityId)
    }

    private fun updateEntityKey(modelId: ModelId, entityId: EntityId, value: EntityKey) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[EntityTable.key] = value
        }
        searchWrite.upsertEntitySearchItem(modelId, entityId)
    }

    private fun updateEntityName(modelId: ModelId, entityId: EntityId, value: LocalizedText?) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[EntityTable.name] = value
        }
        searchWrite.upsertEntitySearchItem(modelId, entityId)
    }

    private fun updateEntityDescription(modelId: ModelId, entityId: EntityId, value: LocalizedMarkdown?) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[EntityTable.description] = value
        }
        searchWrite.upsertEntitySearchItem(modelId, entityId)
    }

    private fun updateEntityIdentifierAttribute(modelId: ModelId, entityId: EntityId, value: AttributeId) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[EntityTable.identifierAttributeSnapshotId] = currentHeadAttributeSnapshotId(modelId, value)
        }
    }

    private fun updateEntityDocumentationHome(modelId: ModelId, entityId: EntityId, value: java.net.URL?) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[EntityTable.documentationHome] = value?.toExternalForm()
        }
    }

    private fun addEntityTag(modelId: ModelId, entityId: EntityId, tagId: TagId) {
        val entitySnapshotId = currentHeadEntitySnapshotId(modelId, entityId)
        val exists = EntityTagTable.select(EntityTagTable.entitySnapshotId).where {
            (EntityTagTable.entitySnapshotId eq entitySnapshotId) and (EntityTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertEntityTag(entitySnapshotId, tagId)
        }
        searchWrite.upsertEntitySearchItem(modelId, entityId)
    }

    private fun insertEntityTag(entityId: EntityId, tagId: TagId) {
        EntityTagTable.insert { row ->
            row[EntityTagTable.entitySnapshotId] = entityId
            row[EntityTagTable.tagId] = tagId
        }
    }

    private fun deleteEntityTag(modelId: ModelId, entityId: EntityId, tagId: TagId) {
        val entitySnapshotId = currentHeadEntitySnapshotId(modelId, entityId)
        EntityTagTable.deleteWhere {
            (EntityTagTable.entitySnapshotId eq entitySnapshotId) and (EntityTagTable.tagId eq tagId)
        }
        searchWrite.upsertEntitySearchItem(modelId, entityId)
    }

    private fun deleteEntity(modelId: ModelId, entityId: EntityId) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        searchWrite.deleteEntityBranch(entityId)
        EntityTable.deleteWhere {
            (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
        }
    }

    private fun insertEntity(cmd: ModelStorageCmd.CreateEntity) {
        val entitySnapshotId = EntityId.generate()
        val identifierAttributeSnapshotId = AttributeId.generate()

        val record = EntityRecord(
            snapshotId = entitySnapshotId,
            lineageId = cmd.entityId,
            modelSnapshotId = currentHeadModelSnapshotId(cmd.modelId),
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
                typeSnapshotId = currentHeadTypeSnapshotId(cmd.modelId, cmd.identityAttributeTypeId),
                optional = cmd.identityAttributeIdOptional
            )
        )
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


    private fun loadEntityAttributes(modelId: ModelId): List<AttributeInMemory> {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        val typeTable = ModelTypeTable.alias("entity_attribute_type_snapshot")
        return EntityAttributeTable.join(
            EntityTable,
            joinType = JoinType.INNER,
            onColumn = EntityAttributeTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).join(
            typeTable,
            joinType = JoinType.INNER,
            onColumn = EntityAttributeTable.typeSnapshotId,
            otherColumn = typeTable[ModelTypeTable.id]
        ).selectAll().where { EntityTable.modelSnapshotId eq modelSnapshotId }.map { row ->
            val record = EntityAttributeRecord.read(row)
            val tags = loadEntityAttributeTags(record.snapshotId)
            toEntityAttribute(
                record,
                tags,
                row[typeTable[ModelTypeTable.lineageId]],
                row[EntityTable.lineageId]
            )
        }
    }

    private fun loadEntityAttributeTags(attributeId: AttributeId): List<TagId> {
        return EntityAttributeTagTable.selectAll().where { EntityAttributeTagTable.attributeSnapshotId eq attributeId }
            .orderBy(EntityAttributeTagTable.tagId to SortOrder.ASC).map { it[EntityAttributeTagTable.tagId] }
    }

    private fun createEntityAttribute(cmd: ModelStorageCmd.CreateEntityAttribute) {
        insertEntityAttribute(
            EntityAttributeRecord(
                snapshotId = AttributeId.generate(),
                lineageId = cmd.attributeId,
                entitySnapshotId = currentHeadEntitySnapshotId(cmd.modelId, cmd.entityId),
                key = cmd.key,
                name = cmd.name,
                description = cmd.description,
                typeSnapshotId = currentHeadTypeSnapshotId(cmd.modelId, cmd.typeId),
                optional = cmd.optional
            )
        )
    }

    private fun updateEntityAttributeKey(entityId: EntityId, attributeId: AttributeId, value: AttributeKey) {
        val modelId = modelIdForEntity(entityId)
        val entitySnapshotId = currentHeadEntitySnapshotId(modelId, entityId)
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.lineageId eq attributeId) and (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
            }) { row ->
            row[EntityAttributeTable.key] = value
        }
        searchWrite.upsertEntityAttributeSearchItem(modelId, attributeId)
    }

    private fun updateEntityAttributeName(entityId: EntityId, attributeId: AttributeId, value: LocalizedText?) {
        val modelId = modelIdForEntity(entityId)
        val entitySnapshotId = currentHeadEntitySnapshotId(modelId, entityId)
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.lineageId eq attributeId) and (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
            }) { row ->
            row[EntityAttributeTable.name] = value
        }
        searchWrite.upsertEntityAttributeSearchItem(modelId, attributeId)
    }

    private fun updateEntityAttributeDescription(
        entityId: EntityId, attributeId: AttributeId, value: LocalizedMarkdown?
    ) {
        val modelId = modelIdForEntity(entityId)
        val entitySnapshotId = currentHeadEntitySnapshotId(modelId, entityId)
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.lineageId eq attributeId) and (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
            }) { row ->
            row[EntityAttributeTable.description] = value
        }
        searchWrite.upsertEntityAttributeSearchItem(modelId, attributeId)

    }

    private fun updateEntityAttributeType(entityId: EntityId, attributeId: AttributeId, value: TypeId) {
        val modelId = modelIdForEntity(entityId)
        val entitySnapshotId = currentHeadEntitySnapshotId(modelId, entityId)
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.lineageId eq attributeId) and (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
            }) { row ->
            row[EntityAttributeTable.typeSnapshotId] = currentHeadTypeSnapshotId(modelId, value)
        }

    }

    private fun updateEntityAttributeOptional(entityId: EntityId, attributeId: AttributeId, value: Boolean) {
        val modelId = modelIdForEntity(entityId)
        val entitySnapshotId = currentHeadEntitySnapshotId(modelId, entityId)
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.lineageId eq attributeId) and (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
            }) { row ->
            row[EntityAttributeTable.optional] = value
        }
    }


    private fun insertEntityAttribute(
        record: EntityAttributeRecord

    ) {
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
        searchWrite.upsertEntityAttributeSearchItem(modelIdForEntitySnapshot(record.entitySnapshotId), record.lineageId)
    }


    private fun addEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        val modelId = modelIdForEntityAttribute(attributeId)
        val attributeSnapshotId = currentHeadAttributeSnapshotId(modelId, attributeId)
        val exists = EntityAttributeTagTable.select(EntityAttributeTagTable.attributeSnapshotId).where {
            (EntityAttributeTagTable.attributeSnapshotId eq attributeSnapshotId) and (EntityAttributeTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertEntityAttributeTag(attributeSnapshotId, tagId)
        }
        searchWrite.upsertEntityAttributeSearchItem(modelId, attributeId)
    }

    private fun insertEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        EntityAttributeTagTable.insert { row ->
            row[EntityAttributeTagTable.attributeSnapshotId] = attributeId
            row[EntityAttributeTagTable.tagId] = tagId
        }
    }

    private fun deleteEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        val modelId = modelIdForEntityAttribute(attributeId)
        val attributeSnapshotId = currentHeadAttributeSnapshotId(modelId, attributeId)
        EntityAttributeTagTable.deleteWhere {
            (EntityAttributeTagTable.attributeSnapshotId eq attributeSnapshotId) and (EntityAttributeTagTable.tagId eq tagId)
        }
        searchWrite.upsertEntityAttributeSearchItem(modelId, attributeId)
    }

    private fun deleteEntityAttribute(entityId: EntityId, attributeId: AttributeId) {
        val modelId = modelIdForEntity(entityId)
        val entitySnapshotId = currentHeadEntitySnapshotId(modelId, entityId)
        searchWrite.deleteEntityAttributeSearchItem(attributeId)
        EntityAttributeTable.deleteWhere {
            (EntityAttributeTable.lineageId eq attributeId) and (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
        }
    }


    // Relationship
    // ------------------------------------------------------------------------


    private fun loadRelationships(modelId: ModelId): List<RelationshipInMemory> {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
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

        return RelationshipTable.selectAll().where { RelationshipTable.modelSnapshotId eq modelSnapshotId }
            .orderBy(RelationshipTable.key to SortOrder.ASC).map { row ->
                val relationshipRecord = RelationshipRecord.read(row)
                val relationshipId = relationshipRecord.snapshotId
                val roles = (roleRowsByRelationshipId[relationshipId] ?: emptyList()).map { roleRow ->
                    toRelationshipRole(
                        RelationshipRoleRecord.read(roleRow),
                        roleRow[roleEntityTable[EntityTable.lineageId]]
                    )
                }
                val tags = loadRelationshipTags(relationshipRecord.snapshotId)
                toRelationship(relationshipRecord, roles, tags)
            }
    }

    private fun loadRelationshipTags(relationshipId: RelationshipId): List<TagId> {
        return RelationshipTagTable.selectAll().where { RelationshipTagTable.relationshipSnapshotId eq relationshipId }
            .orderBy(RelationshipTagTable.tagId to SortOrder.ASC).map { it[RelationshipTagTable.tagId] }
    }

    private fun createRelationship(cmd: ModelStorageCmd.CreateRelationship) {
        val record = RelationshipRecord(
            snapshotId = RelationshipId.generate(),
            lineageId = cmd.relationshipId,
            modelSnapshotId = currentHeadModelSnapshotId(cmd.modelId),
            key = cmd.key,
            name = cmd.name,
            description = cmd.description
        )
        val roles = cmd.roles.map { role ->
            RelationshipRoleRecord(
                snapshotId = RelationshipRoleId.generate(),
                lineageId = role.id,
                relationshipSnapshotId = record.snapshotId,
                key = role.key,
                name = role.name,
                entitySnapshotId = currentHeadEntitySnapshotId(cmd.modelId, role.entityId),
                cardinality = role.cardinality.code
            )
        }
        insertRelationship(record, roles)
        searchWrite.upsertRelationshipSearchItem(cmd.modelId, cmd.relationshipId)
    }

    private fun updateRelationshipKey(relationshipId: RelationshipId, value: RelationshipKey) {
        val modelId = modelIdForRelationship(relationshipId)
        val relationshipSnapshotId = currentHeadRelationshipSnapshotId(modelId, relationshipId)
        RelationshipTable.update(where = { RelationshipTable.id eq relationshipSnapshotId }) { row ->
            row[RelationshipTable.key] = value
        }
        searchWrite.upsertRelationshipSearchItem(modelId, relationshipId)
    }

    private fun updateRelationshipName(relationshipId: RelationshipId, value: LocalizedText?) {
        val modelId = modelIdForRelationship(relationshipId)
        val relationshipSnapshotId = currentHeadRelationshipSnapshotId(modelId, relationshipId)
        RelationshipTable.update(where = { RelationshipTable.id eq relationshipSnapshotId }) { row ->
            row[RelationshipTable.name] = value
        }
        searchWrite.upsertRelationshipSearchItem(modelId, relationshipId)
    }

    private fun updateRelationshipDescription(relationshipId: RelationshipId, value: LocalizedMarkdown?) {
        val modelId = modelIdForRelationship(relationshipId)
        val relationshipSnapshotId = currentHeadRelationshipSnapshotId(modelId, relationshipId)
        RelationshipTable.update(where = { RelationshipTable.id eq relationshipSnapshotId }) { row ->
            row[RelationshipTable.description] = value
        }
        searchWrite.upsertRelationshipSearchItem(modelId, relationshipId)
    }

    private fun createRelationshipRole(cmd: ModelStorageCmd.CreateRelationshipRole) {
        RelationshipRoleTable.insert { row ->
            row[RelationshipRoleTable.id] = RelationshipRoleId.generate()
            row[RelationshipRoleTable.lineageId] = cmd.relationshipRoleId
            row[RelationshipRoleTable.relationshipSnapshotId] = currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
            row[RelationshipRoleTable.key] = cmd.key
            row[RelationshipRoleTable.entitySnapshotId] = currentHeadEntitySnapshotId(cmd.modelId, cmd.entityId)
            row[RelationshipRoleTable.name] = cmd.name
            row[RelationshipRoleTable.cardinality] = cmd.cardinality.code
        }
    }

    private fun updateRelationshipRoleKey(relationshipRoleId: RelationshipRoleId, value: RelationshipRoleKey) {
        RelationshipRoleTable.update(where = { RelationshipRoleTable.lineageId eq relationshipRoleId }) { row ->
            row[RelationshipRoleTable.key] = value
        }
    }

    private fun updateRelationshipRoleName(relationshipRoleId: RelationshipRoleId, value: LocalizedText?) {
        RelationshipRoleTable.update(where = { RelationshipRoleTable.lineageId eq relationshipRoleId }) { row ->
            row[RelationshipRoleTable.name] = value
        }
    }

    private fun updateRelationshipRoleEntity(relationshipRoleId: RelationshipRoleId, value: EntityId) {
        val modelId = modelIdForRelationshipRole(relationshipRoleId)
        RelationshipRoleTable.update(where = { RelationshipRoleTable.lineageId eq relationshipRoleId }) { row ->
            row[RelationshipRoleTable.entitySnapshotId] = currentHeadEntitySnapshotId(modelId, value)
        }
    }

    private fun updateRelationshipRoleCardinality(
        relationshipRoleId: RelationshipRoleId, value: RelationshipCardinality
    ) {
        RelationshipRoleTable.update(where = { RelationshipRoleTable.lineageId eq relationshipRoleId }) { row ->
            row[RelationshipRoleTable.cardinality] = value.code
        }
    }

    private fun deleteRelationshipRole(relationshipId: RelationshipId, relationshipRoleId: RelationshipRoleId) {
        val modelId = modelIdForRelationship(relationshipId)
        val relationshipSnapshotId = currentHeadRelationshipSnapshotId(modelId, relationshipId)
        RelationshipRoleTable.deleteWhere {
            (RelationshipRoleTable.lineageId eq relationshipRoleId) and (RelationshipRoleTable.relationshipSnapshotId eq relationshipSnapshotId)
        }
    }

    private fun addRelationshipTag(relationshipId: RelationshipId, tagId: TagId) {
        val modelId = modelIdForRelationship(relationshipId)
        val relationshipSnapshotId = currentHeadRelationshipSnapshotId(modelId, relationshipId)
        val exists = RelationshipTagTable.select(RelationshipTagTable.relationshipSnapshotId).where {
            (RelationshipTagTable.relationshipSnapshotId eq relationshipSnapshotId) and (RelationshipTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertRelationshipTag(relationshipSnapshotId, tagId)
        }
        searchWrite.upsertRelationshipSearchItem(modelId, relationshipId)
    }

    private fun insertRelationshipTag(
        relationshipId: RelationshipId,
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

    private fun deleteRelationship(modelId: ModelId, relationshipId: RelationshipId) {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        searchWrite.deleteRelationshipBranch(relationshipId)
        RelationshipTable.deleteWhere {
            (RelationshipTable.lineageId eq relationshipId) and (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }
    }

    private fun deleteRelationshipTag(relationshipId: RelationshipId, tagId: TagId) {
        val modelId = modelIdForRelationship(relationshipId)
        val relationshipSnapshotId = currentHeadRelationshipSnapshotId(modelId, relationshipId)
        RelationshipTagTable.deleteWhere {
            (RelationshipTagTable.relationshipSnapshotId eq relationshipSnapshotId) and (RelationshipTagTable.tagId eq tagId)
        }
        searchWrite.upsertRelationshipSearchItem(modelId, relationshipId)
    }
    // Relationship attribute
    // ------------------------------------------------------------------------

    private fun loadRelationshipAttributes(modelId: ModelId): List<AttributeInMemory> {
        val modelSnapshotId = currentHeadModelSnapshotId(modelId)
        val typeTable = ModelTypeTable.alias("relationship_attribute_type_snapshot")
        return RelationshipTable.join(
            RelationshipAttributeTable,
            joinType = JoinType.INNER,
            onColumn = RelationshipTable.id,
            otherColumn = RelationshipAttributeTable.relationshipSnapshotId
        ).join(
            typeTable,
            joinType = JoinType.INNER,
            onColumn = RelationshipAttributeTable.typeSnapshotId,
            otherColumn = typeTable[ModelTypeTable.id]
        ).selectAll().where { RelationshipTable.modelSnapshotId eq modelSnapshotId }.map { row ->
            val record = RelationshipAttributeRecord.read(row)
            val tags = loadRelationshipAttributeTags(record.snapshotId)
            toRelationshipAttribute(
                record,
                tags,
                row[typeTable[ModelTypeTable.lineageId]],
                row[RelationshipTable.lineageId]
            )
        }
    }

    private fun loadRelationshipAttributeTags(attributeId: AttributeId): List<TagId> {
        return RelationshipAttributeTagTable.selectAll()
            .where { RelationshipAttributeTagTable.attributeSnapshotId eq attributeId }
            .orderBy(RelationshipAttributeTagTable.tagId to SortOrder.ASC)
            .map { it[RelationshipAttributeTagTable.tagId] }
    }

    private fun createRelationshipAttribute(cmd: ModelStorageCmd.CreateRelationshipAttribute) {
        val record = RelationshipAttributeRecord(
            snapshotId = AttributeId.generate(),
            lineageId = cmd.attributeId,
            relationshipSnapshotId = currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId),
            name = cmd.name,
            key = cmd.key,
            description = cmd.description,
            typeSnapshotId = currentHeadTypeSnapshotId(cmd.modelId, cmd.typeId),
            optional = cmd.optional
        )
        insertRelationshipAttribute(record)
        searchWrite.upsertRelationshipAttributeSearchItem(cmd.modelId, record.lineageId)
    }

    private fun updateRelationshipAttributeKey(
        relationshipId: RelationshipId, attributeId: AttributeId, value: AttributeKey
    ) {
        val modelId = modelIdForRelationship(relationshipId)
        val relationshipSnapshotId = currentHeadRelationshipSnapshotId(modelId, relationshipId)
        RelationshipAttributeTable.update(where = {
            (RelationshipAttributeTable.lineageId eq attributeId) and
                    (RelationshipAttributeTable.relationshipSnapshotId eq relationshipSnapshotId)
        }) { row ->
            row[RelationshipAttributeTable.key] = value
        }
        searchWrite.upsertRelationshipAttributeSearchItem(modelId, attributeId)

    }

    private fun updateRelationshipAttributeName(
        relationshipId: RelationshipId, attributeId: AttributeId, value: LocalizedText?
    ) {
        val modelId = modelIdForRelationship(relationshipId)
        val relationshipSnapshotId = currentHeadRelationshipSnapshotId(modelId, relationshipId)
        RelationshipAttributeTable.update(
            where = {
                (RelationshipAttributeTable.lineageId eq attributeId) and
                        (RelationshipAttributeTable.relationshipSnapshotId eq relationshipSnapshotId)
            }) { row ->
            row[RelationshipAttributeTable.name] = value
        }
        searchWrite.upsertRelationshipAttributeSearchItem(modelId, attributeId)
    }

    private fun updateRelationshipAttributeDescription(
        relationshipId: RelationshipId, attributeId: AttributeId, value: LocalizedMarkdown?
    ) {
        val modelId = modelIdForRelationship(relationshipId)
        val relationshipSnapshotId = currentHeadRelationshipSnapshotId(modelId, relationshipId)
        RelationshipAttributeTable.update(
            where = {
                (RelationshipAttributeTable.lineageId eq attributeId) and
                        (RelationshipAttributeTable.relationshipSnapshotId eq relationshipSnapshotId)
            }) { row ->
            row[RelationshipAttributeTable.description] = value
        }
        searchWrite.upsertRelationshipAttributeSearchItem(modelId, attributeId)
    }

    private fun updateRelationshipAttributeType(
        relationshipId: RelationshipId, attributeId: AttributeId, value: TypeId
    ) {
        val modelId = modelIdForRelationship(relationshipId)
        val relationshipSnapshotId = currentHeadRelationshipSnapshotId(modelId, relationshipId)
        RelationshipAttributeTable.update(
            where = {
                (RelationshipAttributeTable.lineageId eq attributeId) and
                        (RelationshipAttributeTable.relationshipSnapshotId eq relationshipSnapshotId)
            }) { row ->
            row[RelationshipAttributeTable.typeSnapshotId] = currentHeadTypeSnapshotId(modelId, value)
        }
    }

    private fun updateRelationshipAttributeOptional(
        relationshipId: RelationshipId, attributeId: AttributeId, value: Boolean
    ) {
        val modelId = modelIdForRelationship(relationshipId)
        val relationshipSnapshotId = currentHeadRelationshipSnapshotId(modelId, relationshipId)
        RelationshipAttributeTable.update(
            where = {
                (RelationshipAttributeTable.lineageId eq attributeId) and
                        (RelationshipAttributeTable.relationshipSnapshotId eq relationshipSnapshotId)
            }) { row ->
            row[RelationshipAttributeTable.optional] = value
        }
    }

    private fun addRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        val modelId = modelIdForRelationshipAttribute(attributeId)
        val attributeSnapshotId = currentHeadAttributeSnapshotId(modelId, attributeId)
        val exists = RelationshipAttributeTagTable.select(RelationshipAttributeTagTable.attributeSnapshotId).where {
            (RelationshipAttributeTagTable.attributeSnapshotId eq attributeSnapshotId) and (RelationshipAttributeTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertRelationshipAttributeTag(attributeSnapshotId, tagId)
        }
        searchWrite.upsertRelationshipAttributeSearchItem(modelId, attributeId)
    }

    private fun insertRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
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

    private fun deleteRelationshipAttribute(modelId: ModelId, relationshipId: RelationshipId, attributeId: AttributeId) {
        val relationshipSnapshotId = currentHeadRelationshipSnapshotId(modelId, relationshipId)
        searchWrite.deleteRelationshipAttributeSearchItem(attributeId)
        RelationshipAttributeTable.deleteWhere {
            (RelationshipAttributeTable.lineageId eq attributeId) and
                    (RelationshipAttributeTable.relationshipSnapshotId eq relationshipSnapshotId)
        }
    }

    private fun deleteRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        val modelId = modelIdForRelationshipAttribute(attributeId)
        val attributeSnapshotId = currentHeadAttributeSnapshotId(modelId, attributeId)
        RelationshipAttributeTagTable.deleteWhere {
            (RelationshipAttributeTagTable.attributeSnapshotId eq attributeSnapshotId) and (RelationshipAttributeTagTable.tagId eq tagId)
        }
        searchWrite.upsertRelationshipAttributeSearchItem(modelId, attributeId)
    }

    private fun modelReleaseEventType(): String {
        return eventSystem.registry.findEntryByCmdClass(ModelStorageCmd.ModelRelease::class).eventType
    }

    companion object {
        private const val CURRENT_HEAD_SNAPSHOT_KIND = "CURRENT_HEAD"
        private const val VERSION_SNAPSHOT_KIND = "VERSION_SNAPSHOT"
        private val logger: Logger = LoggerFactory.getLogger(ModelStorageDb::class.java)
    }


}
