package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.search.SearchResults
import io.medatarun.model.infra.db.aggregate.ModelStorageDbSnapshots
import io.medatarun.model.infra.db.events.ModelEventStreamNumberContext
import io.medatarun.model.infra.db.events.ModelEventSystem
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.records.EntityAttributeRecord
import io.medatarun.model.infra.db.records.EntityRecord
import io.medatarun.model.infra.db.tables.*
import io.medatarun.model.infra.db.tables.EntityAttributeTable
import io.medatarun.model.infra.db.tables.EntityAttributeTagTable
import io.medatarun.model.infra.db.tables.RelationshipAttributeTable
import io.medatarun.model.infra.db.tables.RelationshipAttributeTagTable
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.ports.needs.*
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ModelStorageDb(
    private val db: DbConnectionFactory,
    private val clock: ModelClock
) : ModelStorage {

    private val searchRead = ModelStorageDbSearchRead(db)
    private val searchWrite = ModelStorageDbSearchWrite(db)
    private val eventSystem = ModelEventSystem()
    private val snapshots = ModelStorageDbSnapshots()
    private val read = ModelStorageDbRead(db, snapshots, eventSystem.registry)

    override fun existsModelById(id: ModelId): Boolean = read.existsModelById(id)
    override fun existsModelByKey(key: ModelKey): Boolean = read.existsModelByKey(key)
    override fun findAllModelIds(): List<ModelId> = read.findAllModelIds()
    override fun findModelByKeyOptional(key: ModelKey): Model? = read.findModelByKeyOptional(key)
    override fun findModelByIdOptional(id: ModelId): Model? = read.findModelByIdOptional(id)
    override fun findLatestModelReleaseVersionOptional(modelId: ModelId): ModelVersion? = read.findLatestModelReleaseVersionOptional(modelId)
    override fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate? = read.findModelAggregateByIdOptional(id)
    override fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate? = read.findModelAggregateByKeyOptional(key)
    override fun findTypeByIdOptional(modelId: ModelId, typeId: TypeId): ModelType?  = read.findTypeByIdOptional(modelId, typeId)
    override fun findTypeByKeyOptional(modelId: ModelId, key: TypeKey): ModelType? = read.findTypeByKeyOptional(modelId, key)
    override fun findEntityByIdOptional(modelId: ModelId, entityId: EntityId): Entity?  = read.findEntityByIdOptional(modelId, entityId)
    override fun findEntityByKeyOptional(modelId: ModelId, entityKey: EntityKey): Entity? = read.findEntityByKeyOptional(modelId, entityKey)
    override fun findEntityAttributeByIdOptional(modelId: ModelId, entityId: EntityId, attributeId: AttributeId): Attribute?  = read.findEntityAttributeByIdOptional(modelId, entityId, attributeId)
    override fun findEntityAttributeByKeyOptional(modelId: ModelId, entityId: EntityId, key: AttributeKey): Attribute? = read.findEntityAttributeByKeyOptional(modelId, entityId, key)
    override fun findRelationshipByIdOptional(modelId: ModelId, relationshipId: RelationshipId): Relationship? = read.findRelationshipByIdOptional(modelId, relationshipId)
    override fun findRelationshipByKeyOptional(modelId: ModelId, relationshipKey: RelationshipKey): Relationship? = read.findRelationshipByKeyOptional(modelId, relationshipKey)
    override fun findRelationshipRoleByIdOptional(modelId: ModelId, relationshipId: RelationshipId, roleId: RelationshipRoleId): RelationshipRole? = read.findRelationshipRoleByIdOptional(modelId, relationshipId, roleId)
    override fun findRelationshipRoleByKeyOptional(modelId: ModelId, relationshipId: RelationshipId, roleKey: RelationshipRoleKey): RelationshipRole? = read.findRelationshipRoleByKeyOptional(modelId, relationshipId, roleKey)
    override fun findRelationshipAttributeByIdOptional(modelId: ModelId, relationshipId: RelationshipId, attributeId: AttributeId): Attribute? = read.findRelationshipAttributeByIdOptional(modelId, relationshipId, attributeId)
    override fun findRelationshipAttributeByKeyOptional(modelId: ModelId, relationshipId: RelationshipId, key: AttributeKey): Attribute?  = read.findRelationshipAttributeByKeyOptional(modelId, relationshipId, key)
    override fun isTypeUsedInEntityAttributes(modelId: ModelId, typeId: TypeId): Boolean  = read.isTypeUsedInEntityAttributes(modelId, typeId)
    override fun isTypeUsedInRelationshipAttributes(modelId: ModelId, typeId: TypeId): Boolean  = read.isTypeUsedInRelationshipAttributes(modelId, typeId)

    fun findAllModelEvents(modelId: ModelId) = read.findAllModelEvents(modelId)

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    override fun search(query: ModelStorageSearchQuery): SearchResults {
        return searchRead.search(query)
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
            is ModelStorageCmd.UpdateModelName -> updateModelName(cmd)
            is ModelStorageCmd.UpdateModelKey -> updateModelKey(cmd)
            is ModelStorageCmd.UpdateModelDescription -> updateModelDescription(cmd)
            is ModelStorageCmd.UpdateModelAuthority -> updateModelAuthority(cmd)
            is ModelStorageCmd.ModelRelease -> releaseModel(cmd)
            is ModelStorageCmd.UpdateModelDocumentationHome -> updateModelDocumentationHome(cmd)
            is ModelStorageCmd.UpdateModelTagAdd -> addModelTag(cmd)
            is ModelStorageCmd.UpdateModelTagDelete -> deleteModelTag(cmd)
            is ModelStorageCmd.CreateType -> createType(cmd)
            is ModelStorageCmd.UpdateTypeKey -> updateTypeKey(cmd)
            is ModelStorageCmd.UpdateTypeName -> updateTypeName(cmd)
            is ModelStorageCmd.UpdateTypeDescription -> updateTypeDescription(cmd)
            is ModelStorageCmd.DeleteType -> deleteType(cmd)
            is ModelStorageCmd.CreateEntity -> createEntity(cmd)
            is ModelStorageCmd.UpdateEntityKey -> updateEntityKey(cmd)
            is ModelStorageCmd.UpdateEntityName -> updateEntityName(cmd)
            is ModelStorageCmd.UpdateEntityDescription -> updateEntityDescription(cmd)
            is ModelStorageCmd.UpdateEntityIdentifierAttribute -> updateEntityIdentifierAttribute(cmd)
            is ModelStorageCmd.UpdateEntityDocumentationHome -> updateEntityDocumentationHome(cmd)
            is ModelStorageCmd.UpdateEntityTagAdd -> addEntityTag(cmd)
            is ModelStorageCmd.UpdateEntityTagDelete -> deleteEntityTag(cmd)
            is ModelStorageCmd.DeleteEntity -> deleteEntity(cmd)
            is ModelStorageCmd.CreateEntityAttribute -> createEntityAttribute(cmd)
            is ModelStorageCmd.UpdateEntityAttributeKey -> updateEntityAttributeKey(cmd)
            is ModelStorageCmd.UpdateEntityAttributeName -> updateEntityAttributeName(cmd)
            is ModelStorageCmd.UpdateEntityAttributeDescription -> updateEntityAttributeDescription(cmd)
            is ModelStorageCmd.UpdateEntityAttributeType -> updateEntityAttributeType(cmd)
            is ModelStorageCmd.UpdateEntityAttributeOptional -> updateEntityAttributeOptional(cmd)
            is ModelStorageCmd.UpdateEntityAttributeTagAdd -> addEntityAttributeTag(cmd)
            is ModelStorageCmd.UpdateEntityAttributeTagDelete -> deleteEntityAttributeTag(cmd)
            is ModelStorageCmd.DeleteEntityAttribute -> deleteEntityAttribute(cmd)
            is ModelStorageCmd.CreateRelationship -> createRelationship(cmd)
            is ModelStorageCmd.UpdateRelationshipKey -> updateRelationshipKey(cmd)
            is ModelStorageCmd.UpdateRelationshipName -> updateRelationshipName(cmd)
            is ModelStorageCmd.UpdateRelationshipDescription -> updateRelationshipDescription(cmd)
            is ModelStorageCmd.CreateRelationshipRole -> createRelationshipRole(cmd)
            is ModelStorageCmd.UpdateRelationshipRoleKey -> updateRelationshipRoleKey(cmd)
            is ModelStorageCmd.UpdateRelationshipRoleName -> updateRelationshipRoleName(cmd)
            is ModelStorageCmd.UpdateRelationshipRoleEntity -> updateRelationshipRoleEntity(cmd)
            is ModelStorageCmd.UpdateRelationshipRoleCardinality -> updateRelationshipRoleCardinality(cmd)
            is ModelStorageCmd.UpdateRelationshipTagAdd -> addRelationshipTag(cmd)
            is ModelStorageCmd.UpdateRelationshipTagDelete -> deleteRelationshipTag(cmd)
            is ModelStorageCmd.DeleteRelationship -> deleteRelationship(cmd)
            is ModelStorageCmd.DeleteRelationshipRole -> deleteRelationshipRole(cmd)
            is ModelStorageCmd.CreateRelationshipAttribute -> createRelationshipAttribute(cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeKey -> updateRelationshipAttributeKey(cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeName -> updateRelationshipAttributeName(cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeDescription -> updateRelationshipAttributeDescription(cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeType -> updateRelationshipAttributeType(cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeOptional -> updateRelationshipAttributeOptional(cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeTagAdd -> addRelationshipAttributeTag(cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeTagDelete -> deleteRelationshipAttributeTag(cmd)
            is ModelStorageCmd.DeleteRelationshipAttribute -> deleteRelationshipAttribute(cmd)
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




    /**
     * Keeps the projected CURRENT_HEAD metadata aligned with the latest event
     * revision that has been applied.
     */
    private fun updateCurrentHeadProjectionMetadata(modelId: ModelId, upToRevision: Int) {
        val now = clock.now().toString()
        ModelSnapshotTable.update(where = { snapshots.currentHeadModelSnapshotCriteria(modelId) }) { row ->
            row[ModelSnapshotTable.upToRevision] = upToRevision
            row[ModelSnapshotTable.updatedAt] = now
        }
    }


    // Model
    // ------------------------------------------------------------------------


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

    private fun updateModelName(cmd: ModelStorageCmd.UpdateModelName) {
        ModelSnapshotTable.update(where = { snapshots.currentHeadModelSnapshotCriteria(cmd.modelId) }) { row ->
            row[ModelSnapshotTable.name] = cmd.name
        }
        searchWrite.upsertModelSearchItem(cmd.modelId)
    }

    private fun updateModelKey(cmd: ModelStorageCmd.UpdateModelKey) {
        ModelSnapshotTable.update(where = { snapshots.currentHeadModelSnapshotCriteria(cmd.modelId) }) { row ->
            row[ModelSnapshotTable.key] = cmd.key
        }
        searchWrite.upsertModelSearchItem(cmd.modelId)
    }

    private fun updateModelDescription(cmd: ModelStorageCmd.UpdateModelDescription) {
        ModelSnapshotTable.update(where = { snapshots.currentHeadModelSnapshotCriteria(cmd.modelId) }) { row ->
            row[ModelSnapshotTable.description] = cmd.description
        }
        searchWrite.upsertModelSearchItem(cmd.modelId)
    }

    private fun updateModelAuthority(cmd: ModelStorageCmd.UpdateModelAuthority) {
        ModelSnapshotTable.update(where = { snapshots.currentHeadModelSnapshotCriteria(cmd.modelId) }) { row ->
            row[ModelSnapshotTable.authority] = cmd.authority
        }
    }

    private fun releaseModel(cmd: ModelStorageCmd.ModelRelease) {
        ModelSnapshotTable.update(where = { snapshots.currentHeadModelSnapshotCriteria(cmd.modelId) }) { row ->
            row[ModelSnapshotTable.version] = cmd.version.value
        }
        createVersionSnapshotFromCurrentHead(cmd.modelId, cmd.version)
    }

    private fun updateModelDocumentationHome(cmd: ModelStorageCmd.UpdateModelDocumentationHome) {
        ModelSnapshotTable.update(where = { snapshots.currentHeadModelSnapshotCriteria(cmd.modelId) }) { row ->
            row[ModelSnapshotTable.documentationHome] = cmd.url?.toExternalForm()
        }
    }

    private fun addModelTag(cmd: ModelStorageCmd.UpdateModelTagAdd) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        val exists = ModelTagTable.select(ModelTagTable.modelSnapshotId).where {
            (ModelTagTable.modelSnapshotId eq modelSnapshotId) and (ModelTagTable.tagId eq cmd.tagId)
        }.limit(1).any()
        if (!exists) {
            ModelTagTable.insert { row ->
                row[ModelTagTable.modelSnapshotId] = modelSnapshotId
                row[ModelTagTable.tagId] = cmd.tagId
            }
        }
        searchWrite.upsertModelSearchItem(cmd.modelId)
    }

    private fun deleteModelTag(cmd: ModelStorageCmd.UpdateModelTagDelete) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        ModelTagTable.deleteWhere {
            (ModelTagTable.modelSnapshotId eq modelSnapshotId) and (ModelTagTable.tagId eq cmd.tagId)
        }
        searchWrite.upsertModelSearchItem(cmd.modelId)
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
            row[ModelSnapshotTable.snapshotKind] = snapshots.CURRENT_HEAD_SNAPSHOT_KIND
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
        val currentHeadSnapshotId = snapshots.currentHeadModelSnapshotId(modelId)
        val currentHeadRow =
            ModelSnapshotTable.selectAll().where { snapshots.currentHeadModelSnapshotCriteria(modelId) }.singleOrNull()
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
            row[ModelSnapshotTable.snapshotKind] = snapshots.VERSION_SNAPSHOT_KIND
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
                    (ModelEventTable.eventType eq eventSystem.registry.modelReleaseEventType()) and
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


    private fun createType(cmd: ModelStorageCmd.CreateType) {
        val lineageId = TypeId.generate()
        val record = ModelTypeRecord(
            snapshotId = TypeId.generate(),
            lineageId = lineageId,
            modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId),
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

    private fun updateTypeKey(cmd: ModelStorageCmd.UpdateTypeKey) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq cmd.typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[ModelTypeTable.key] = cmd.value
        }
    }

    private fun updateTypeName(cmd: ModelStorageCmd.UpdateTypeName) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq cmd.typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[ModelTypeTable.name] = cmd.value
        }
    }

    private fun updateTypeDescription(cmd: ModelStorageCmd.UpdateTypeDescription) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq cmd.typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[ModelTypeTable.description] = cmd.value
        }
    }

    private fun deleteType(cmd: ModelStorageCmd.DeleteType) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        ModelTypeTable.deleteWhere {
            (ModelTypeTable.lineageId eq cmd.typeId) and (ModelTypeTable.modelSnapshotId eq modelSnapshotId)
        }
    }

    // Entity
    // ------------------------------------------------------------------------


    private fun createEntity(cmd: ModelStorageCmd.CreateEntity) {
        val entitySnapshotId = EntityId.generate()
        val identifierAttributeSnapshotId = AttributeId.generate()
        val record = EntityRecord(
            snapshotId = entitySnapshotId,
            lineageId = cmd.entityId,
            modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId),
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
                typeSnapshotId = snapshots.currentHeadTypeSnapshotId(cmd.modelId, cmd.identityAttributeTypeId),
                optional = cmd.identityAttributeIdOptional
            )
        )
        searchWrite.upsertEntitySearchItem(cmd.modelId, cmd.entityId)
        searchWrite.upsertEntityAttributeSearchItem(cmd.modelId, cmd.identityAttributeId)
    }

    private fun updateEntityKey(cmd: ModelStorageCmd.UpdateEntityKey) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[EntityTable.key] = cmd.value
        }
        searchWrite.upsertEntitySearchItem(cmd.modelId, cmd.entityId)
    }

    private fun updateEntityName(cmd: ModelStorageCmd.UpdateEntityName) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[EntityTable.name] = cmd.value
        }
        searchWrite.upsertEntitySearchItem(cmd.modelId, cmd.entityId)
    }

    private fun updateEntityDescription(cmd: ModelStorageCmd.UpdateEntityDescription) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[EntityTable.description] = cmd.value
        }
        searchWrite.upsertEntitySearchItem(cmd.modelId, cmd.entityId)
    }

    private fun updateEntityIdentifierAttribute(cmd: ModelStorageCmd.UpdateEntityIdentifierAttribute) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[EntityTable.identifierAttributeSnapshotId] = snapshots.currentHeadAttributeSnapshotId(cmd.modelId, cmd.value)
        }
    }

    private fun updateEntityDocumentationHome(cmd: ModelStorageCmd.UpdateEntityDocumentationHome) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
            }) { row ->
            row[EntityTable.documentationHome] = cmd.value?.toExternalForm()
        }
    }

    private fun addEntityTag(cmd: ModelStorageCmd.UpdateEntityTagAdd) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotId(cmd.modelId, cmd.entityId)
        val exists = EntityTagTable.select(EntityTagTable.entitySnapshotId).where {
            (EntityTagTable.entitySnapshotId eq entitySnapshotId) and (EntityTagTable.tagId eq cmd.tagId)
        }.limit(1).any()
        if (!exists) {
            insertEntityTag(entitySnapshotId, cmd.tagId)
        }
        searchWrite.upsertEntitySearchItem(cmd.modelId, cmd.entityId)
    }

    private fun insertEntityTag(entityId: EntityId, tagId: TagId) {
        EntityTagTable.insert { row ->
            row[EntityTagTable.entitySnapshotId] = entityId
            row[EntityTagTable.tagId] = tagId
        }
    }

    private fun deleteEntityTag(cmd: ModelStorageCmd.UpdateEntityTagDelete) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotId(cmd.modelId, cmd.entityId)
        EntityTagTable.deleteWhere {
            (EntityTagTable.entitySnapshotId eq entitySnapshotId) and (EntityTagTable.tagId eq cmd.tagId)
        }
        searchWrite.upsertEntitySearchItem(cmd.modelId, cmd.entityId)
    }

    private fun deleteEntity(cmd: ModelStorageCmd.DeleteEntity) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        searchWrite.deleteEntityBranch(cmd.entityId)
        EntityTable.deleteWhere {
            (EntityTable.lineageId eq cmd.entityId) and (EntityTable.modelSnapshotId eq modelSnapshotId)
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



    private fun createEntityAttribute(cmd: ModelStorageCmd.CreateEntityAttribute) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotId(cmd.modelId, cmd.entityId)
        insertEntityAttribute(
            EntityAttributeRecord(
                snapshotId = AttributeId.generate(),
                lineageId = cmd.attributeId,
                entitySnapshotId = entitySnapshotId,
                key = cmd.key,
                name = cmd.name,
                description = cmd.description,
                typeSnapshotId = snapshots.currentHeadTypeSnapshotId(cmd.modelId, cmd.typeId),
                optional = cmd.optional
            )
        )
        searchWrite.upsertEntityAttributeSearchItem(cmd.modelId, cmd.attributeId)
    }

    private fun updateEntityAttributeKey(cmd: ModelStorageCmd.UpdateEntityAttributeKey) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotId(cmd.modelId, cmd.entityId)
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.lineageId eq cmd.attributeId) and (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
            }) { row ->
            row[EntityAttributeTable.key] = cmd.value
        }
        searchWrite.upsertEntityAttributeSearchItem(cmd.modelId, cmd.attributeId)
    }

    private fun updateEntityAttributeName(cmd: ModelStorageCmd.UpdateEntityAttributeName) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotId(cmd.modelId, cmd.entityId)
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.lineageId eq cmd.attributeId) and (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
            }) { row ->
            row[EntityAttributeTable.name] = cmd.value
        }
        searchWrite.upsertEntityAttributeSearchItem(cmd.modelId, cmd.attributeId)
    }

    private fun updateEntityAttributeDescription(cmd: ModelStorageCmd.UpdateEntityAttributeDescription) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotId(cmd.modelId, cmd.entityId)
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.lineageId eq cmd.attributeId) and (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
            }) { row ->
            row[EntityAttributeTable.description] = cmd.value
        }
        searchWrite.upsertEntityAttributeSearchItem(cmd.modelId, cmd.attributeId)
    }

    private fun updateEntityAttributeType(cmd: ModelStorageCmd.UpdateEntityAttributeType) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotId(cmd.modelId, cmd.entityId)
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.lineageId eq cmd.attributeId) and (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
            }) { row ->
            row[EntityAttributeTable.typeSnapshotId] = snapshots.currentHeadTypeSnapshotId(cmd.modelId, cmd.value)
        }
    }

    private fun updateEntityAttributeOptional(cmd: ModelStorageCmd.UpdateEntityAttributeOptional) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotId(cmd.modelId, cmd.entityId)
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.lineageId eq cmd.attributeId) and (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
            }) { row ->
            row[EntityAttributeTable.optional] = cmd.value
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


    private fun addEntityAttributeTag(cmd: ModelStorageCmd.UpdateEntityAttributeTagAdd) {
        val attributeSnapshotId = snapshots.currentHeadAttributeSnapshotId(cmd.modelId, cmd.attributeId)
        val exists = EntityAttributeTagTable.select(EntityAttributeTagTable.attributeSnapshotId).where {
            (EntityAttributeTagTable.attributeSnapshotId eq attributeSnapshotId) and (EntityAttributeTagTable.tagId eq cmd.tagId)
        }.limit(1).any()
        if (!exists) {
            insertEntityAttributeTag(attributeSnapshotId, cmd.tagId)
        }
        searchWrite.upsertEntityAttributeSearchItem(cmd.modelId, cmd.attributeId)
    }

    private fun insertEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        EntityAttributeTagTable.insert { row ->
            row[EntityAttributeTagTable.attributeSnapshotId] = attributeId
            row[EntityAttributeTagTable.tagId] = tagId
        }
    }

    private fun deleteEntityAttributeTag(cmd: ModelStorageCmd.UpdateEntityAttributeTagDelete) {
        val attributeSnapshotId = snapshots.currentHeadAttributeSnapshotId(cmd.modelId, cmd.attributeId)
        EntityAttributeTagTable.deleteWhere {
            (EntityAttributeTagTable.attributeSnapshotId eq attributeSnapshotId) and (EntityAttributeTagTable.tagId eq cmd.tagId)
        }
        searchWrite.upsertEntityAttributeSearchItem(cmd.modelId, cmd.attributeId)
    }

    private fun deleteEntityAttribute(cmd: ModelStorageCmd.DeleteEntityAttribute) {
        val entitySnapshotId = snapshots.currentHeadEntitySnapshotId(cmd.modelId, cmd.entityId)
        searchWrite.deleteEntityAttributeSearchItem(cmd.attributeId)
        EntityAttributeTable.deleteWhere {
            (EntityAttributeTable.lineageId eq cmd.attributeId) and (EntityAttributeTable.entitySnapshotId eq entitySnapshotId)
        }
    }


    // Relationship
    // ------------------------------------------------------------------------




    private fun createRelationship(cmd: ModelStorageCmd.CreateRelationship) {
        val record = RelationshipRecord(
            snapshotId = RelationshipId.generate(),
            lineageId = cmd.relationshipId,
            modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId),
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
                entitySnapshotId = snapshots.currentHeadEntitySnapshotId(cmd.modelId, role.entityId),
                cardinality = role.cardinality.code
            )
        }
        insertRelationship(record, roles)
        searchWrite.upsertRelationshipSearchItem(cmd.modelId, cmd.relationshipId)
    }

    private fun updateRelationshipKey(cmd: ModelStorageCmd.UpdateRelationshipKey) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipTable.update(where = { RelationshipTable.id eq relationshipSnapshotId }) { row ->
            row[RelationshipTable.key] = cmd.value
        }
        searchWrite.upsertRelationshipSearchItem(cmd.modelId, cmd.relationshipId)
    }

    private fun updateRelationshipName(cmd: ModelStorageCmd.UpdateRelationshipName) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipTable.update(where = { RelationshipTable.id eq relationshipSnapshotId }) { row ->
            row[RelationshipTable.name] = cmd.value
        }
        searchWrite.upsertRelationshipSearchItem(cmd.modelId, cmd.relationshipId)
    }

    private fun updateRelationshipDescription(cmd: ModelStorageCmd.UpdateRelationshipDescription) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipTable.update(where = { RelationshipTable.id eq relationshipSnapshotId }) { row ->
            row[RelationshipTable.description] = cmd.value
        }
        searchWrite.upsertRelationshipSearchItem(cmd.modelId, cmd.relationshipId)
    }

    private fun createRelationshipRole(cmd: ModelStorageCmd.CreateRelationshipRole) {
        RelationshipRoleTable.insert { row ->
            row[RelationshipRoleTable.id] = RelationshipRoleId.generate()
            row[RelationshipRoleTable.lineageId] = cmd.relationshipRoleId
            row[RelationshipRoleTable.relationshipSnapshotId] = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
            row[RelationshipRoleTable.key] = cmd.key
            row[RelationshipRoleTable.entitySnapshotId] = snapshots.currentHeadEntitySnapshotId(cmd.modelId, cmd.entityId)
            row[RelationshipRoleTable.name] = cmd.name
            row[RelationshipRoleTable.cardinality] = cmd.cardinality.code
        }
    }

    private fun updateRelationshipRoleKey(cmd: ModelStorageCmd.UpdateRelationshipRoleKey) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipRoleTable.update(where = {
            (RelationshipRoleTable.lineageId eq cmd.relationshipRoleId) and
                    (RelationshipRoleTable.relationshipSnapshotId eq relationshipSnapshotId)
        }) { row ->
            row[RelationshipRoleTable.key] = cmd.value
        }
    }

    private fun updateRelationshipRoleName(cmd: ModelStorageCmd.UpdateRelationshipRoleName) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipRoleTable.update(where = {
            (RelationshipRoleTable.lineageId eq cmd.relationshipRoleId) and
                    (RelationshipRoleTable.relationshipSnapshotId eq relationshipSnapshotId)
        }) { row ->
            row[RelationshipRoleTable.name] = cmd.value
        }
    }

    private fun updateRelationshipRoleEntity(cmd: ModelStorageCmd.UpdateRelationshipRoleEntity) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipRoleTable.update(where = {
            (RelationshipRoleTable.lineageId eq cmd.relationshipRoleId) and
                    (RelationshipRoleTable.relationshipSnapshotId eq relationshipSnapshotId)
        }) { row ->
            row[RelationshipRoleTable.entitySnapshotId] = snapshots.currentHeadEntitySnapshotId(cmd.modelId, cmd.value)
        }
    }

    private fun updateRelationshipRoleCardinality(cmd: ModelStorageCmd.UpdateRelationshipRoleCardinality) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipRoleTable.update(where = {
            (RelationshipRoleTable.lineageId eq cmd.relationshipRoleId) and
                    (RelationshipRoleTable.relationshipSnapshotId eq relationshipSnapshotId)
        }) { row ->
            row[RelationshipRoleTable.cardinality] = cmd.value.code
        }
    }

    private fun deleteRelationshipRole(cmd: ModelStorageCmd.DeleteRelationshipRole) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipRoleTable.deleteWhere {
            (RelationshipRoleTable.lineageId eq cmd.relationshipRoleId) and
                    (RelationshipRoleTable.relationshipSnapshotId eq relationshipSnapshotId)
        }
    }

    private fun addRelationshipTag(cmd: ModelStorageCmd.UpdateRelationshipTagAdd) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        val exists = RelationshipTagTable.select(RelationshipTagTable.relationshipSnapshotId).where {
            (RelationshipTagTable.relationshipSnapshotId eq relationshipSnapshotId) and
                    (RelationshipTagTable.tagId eq cmd.tagId)
        }.limit(1).any()
        if (!exists) {
            insertRelationshipTag(relationshipSnapshotId, cmd.tagId)
        }
        searchWrite.upsertRelationshipSearchItem(cmd.modelId, cmd.relationshipId)
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

    private fun deleteRelationship(cmd: ModelStorageCmd.DeleteRelationship) {
        val modelSnapshotId = snapshots.currentHeadModelSnapshotId(cmd.modelId)
        searchWrite.deleteRelationshipBranch(cmd.relationshipId)
        RelationshipTable.deleteWhere {
            (RelationshipTable.lineageId eq cmd.relationshipId) and (RelationshipTable.modelSnapshotId eq modelSnapshotId)
        }
    }

    private fun deleteRelationshipTag(cmd: ModelStorageCmd.UpdateRelationshipTagDelete) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipTagTable.deleteWhere {
            (RelationshipTagTable.relationshipSnapshotId eq relationshipSnapshotId) and
                    (RelationshipTagTable.tagId eq cmd.tagId)
        }
        searchWrite.upsertRelationshipSearchItem(cmd.modelId, cmd.relationshipId)
    }
    // Relationship attribute
    // ------------------------------------------------------------------------

    private fun createRelationshipAttribute(cmd: ModelStorageCmd.CreateRelationshipAttribute) {
        val record = RelationshipAttributeRecord(
            snapshotId = AttributeId.generate(),
            lineageId = cmd.attributeId,
            relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId),
            name = cmd.name,
            key = cmd.key,
            description = cmd.description,
            typeSnapshotId = snapshots.currentHeadTypeSnapshotId(cmd.modelId, cmd.typeId),
            optional = cmd.optional
        )
        insertRelationshipAttribute(record)
        searchWrite.upsertRelationshipAttributeSearchItem(cmd.modelId, record.lineageId)
    }

    private fun updateRelationshipAttributeKey(cmd: ModelStorageCmd.UpdateRelationshipAttributeKey) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipAttributeTable.update(where = {
            (RelationshipAttributeTable.lineageId eq cmd.attributeId) and
                    (RelationshipAttributeTable.relationshipSnapshotId eq relationshipSnapshotId)
        }) { row ->
            row[RelationshipAttributeTable.key] = cmd.value
        }
        searchWrite.upsertRelationshipAttributeSearchItem(cmd.modelId, cmd.attributeId)
    }

    private fun updateRelationshipAttributeName(cmd: ModelStorageCmd.UpdateRelationshipAttributeName) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipAttributeTable.update(
            where = {
                (RelationshipAttributeTable.lineageId eq cmd.attributeId) and
                        (RelationshipAttributeTable.relationshipSnapshotId eq relationshipSnapshotId)
            }) { row ->
            row[RelationshipAttributeTable.name] = cmd.value
        }
        searchWrite.upsertRelationshipAttributeSearchItem(cmd.modelId, cmd.attributeId)
    }

    private fun updateRelationshipAttributeDescription(cmd: ModelStorageCmd.UpdateRelationshipAttributeDescription) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipAttributeTable.update(
            where = {
                (RelationshipAttributeTable.lineageId eq cmd.attributeId) and
                        (RelationshipAttributeTable.relationshipSnapshotId eq relationshipSnapshotId)
            }) { row ->
            row[RelationshipAttributeTable.description] = cmd.value
        }
        searchWrite.upsertRelationshipAttributeSearchItem(cmd.modelId, cmd.attributeId)
    }

    private fun updateRelationshipAttributeType(cmd: ModelStorageCmd.UpdateRelationshipAttributeType) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipAttributeTable.update(
            where = {
                (RelationshipAttributeTable.lineageId eq cmd.attributeId) and
                        (RelationshipAttributeTable.relationshipSnapshotId eq relationshipSnapshotId)
            }) { row ->
            row[RelationshipAttributeTable.typeSnapshotId] = snapshots.currentHeadTypeSnapshotId(cmd.modelId, cmd.value)
        }
    }

    private fun updateRelationshipAttributeOptional(cmd: ModelStorageCmd.UpdateRelationshipAttributeOptional) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        RelationshipAttributeTable.update(
            where = {
                (RelationshipAttributeTable.lineageId eq cmd.attributeId) and
                        (RelationshipAttributeTable.relationshipSnapshotId eq relationshipSnapshotId)
            }) { row ->
            row[RelationshipAttributeTable.optional] = cmd.value
        }
    }

    private fun addRelationshipAttributeTag(cmd: ModelStorageCmd.UpdateRelationshipAttributeTagAdd) {
        val attributeSnapshotId = snapshots.currentHeadAttributeSnapshotId(cmd.modelId, cmd.attributeId)
        val exists = RelationshipAttributeTagTable.select(RelationshipAttributeTagTable.attributeSnapshotId).where {
            (RelationshipAttributeTagTable.attributeSnapshotId eq attributeSnapshotId) and (RelationshipAttributeTagTable.tagId eq cmd.tagId)
        }.limit(1).any()
        if (!exists) {
            insertRelationshipAttributeTag(attributeSnapshotId, cmd.tagId)
        }
        searchWrite.upsertRelationshipAttributeSearchItem(cmd.modelId, cmd.attributeId)
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

    private fun deleteRelationshipAttribute(cmd: ModelStorageCmd.DeleteRelationshipAttribute) {
        val relationshipSnapshotId = snapshots.currentHeadRelationshipSnapshotId(cmd.modelId, cmd.relationshipId)
        searchWrite.deleteRelationshipAttributeSearchItem(cmd.attributeId)
        RelationshipAttributeTable.deleteWhere {
            (RelationshipAttributeTable.lineageId eq cmd.attributeId) and
                    (RelationshipAttributeTable.relationshipSnapshotId eq relationshipSnapshotId)
        }
    }

    private fun deleteRelationshipAttributeTag(cmd: ModelStorageCmd.UpdateRelationshipAttributeTagDelete) {
        val attributeSnapshotId = snapshots.currentHeadAttributeSnapshotId(cmd.modelId, cmd.attributeId)
        RelationshipAttributeTagTable.deleteWhere {
            (RelationshipAttributeTagTable.attributeSnapshotId eq attributeSnapshotId) and (RelationshipAttributeTagTable.tagId eq cmd.tagId)
        }
        searchWrite.upsertRelationshipAttributeSearchItem(cmd.modelId, cmd.attributeId)
    }


    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ModelStorageDb::class.java)
    }


}
