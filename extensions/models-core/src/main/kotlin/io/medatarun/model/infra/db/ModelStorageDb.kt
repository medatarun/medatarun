package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.domain.search.SearchResults
import io.medatarun.model.infra.db.aggregate.ModelStorageDbAggregateReader
import io.medatarun.model.infra.db.events.ModelEventStreamNumberContext
import io.medatarun.model.infra.db.events.ModelEventSystem
import io.medatarun.model.infra.db.records.ModelEventRecord
import io.medatarun.model.infra.db.snapshots.ModelStorageDbProjection
import io.medatarun.model.infra.db.snapshots.ModelStorageDbProjection.ProjectionEventCtx
import io.medatarun.model.infra.db.snapshots.ModelStorageDbSnapshotCreate
import io.medatarun.model.infra.db.snapshots.ModelStorageDbSnapshotWriter
import io.medatarun.model.infra.db.snapshots.ModelStorageDbSnapshotHead
import io.medatarun.model.infra.db.snapshots.SnapshotSelector.CurrentHeadByModelId
import io.medatarun.model.infra.db.tables.ModelEventTable
import io.medatarun.model.infra.db.tables.ModelSnapshotTable
import io.medatarun.model.infra.db.tables.ModelTable
import io.medatarun.model.ports.needs.*
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.storage.eventsourcing.StorageEventEncoded
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ModelStorageDb(
    private val db: DbConnectionFactory,
    private val clock: ModelClock
) : ModelStorage {

    private val searchRead = ModelStorageDbSearchRead()
    private val searchWrite = ModelStorageDbSearchWrite()
    private val eventSystem = ModelEventSystem()
    private val snapshotHead = ModelStorageDbSnapshotHead()
    private val snapshotWriter = ModelStorageDbSnapshotWriter(snapshotHead, clock)
    private val snapshotCreate = ModelStorageDbSnapshotCreate(clock, snapshotWriter)
    private val aggregateReader = ModelStorageDbAggregateReader()
    private val read = ModelStorageDbRead(eventSystem.registry, aggregateReader)
    private val projection = ModelStorageDbProjection(
        searchWrite = searchWrite,
        snapshotHead = snapshotHead,
        clock = clock,
        snapWrite = snapshotWriter,
        snapshotCreate = snapshotCreate
    )

    override fun existsModelById(id: ModelId): Boolean {
        return db.withExposed {
            logger.debug("existsModelById id={}", id)
            read.existsModelById(id)
        }
    }

    override fun existsModelByKey(key: ModelKey): Boolean {
        return db.withExposed {
            logger.debug("existsModelByKey key={}", key)
            read.existsModelByKey(key)
        }
    }

    override fun findAllModelIds(): List<ModelId> {
        return db.withExposed {
            logger.debug("findAllModelIds")
            read.findAllModelIds()
        }
    }

    override fun findModelByKeyOptional(key: ModelKey): Model? {
        return db.withExposed {
            logger.debug("findModelByKeyOptional key={}", key)
            read.findModelByKeyOptional(key)
        }
    }

    override fun findModelByIdOptional(id: ModelId): Model? {
        return db.withExposed {
            logger.debug("findModelByIdOptional id={}", id)
            read.findModelByIdOptional(id)
        }
    }

    override fun findModelTags(id: ModelId): List<TagId> {
        return db.withExposed {
            logger.debug("findModelTags id={}", id)
            read.findAllModelTags(id)
        }
    }

    override fun findLatestModelReleaseVersionOptional(modelId: ModelId): ModelVersion? {
        return db.withExposed {
            logger.debug("findLatestModelReleaseVersionOptional modelId={}", modelId)
            read.findLatestModelReleaseVersionOptional(modelId)
        }
    }

    override fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate? {
        return db.withExposed {
            logger.debug("findModelAggregateByIdOptional id={}", id)
            read.findModelAggregateByIdOptional(id)
        }
    }

    override fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate? {
        return db.withExposed {
            logger.debug("findModelAggregateByKeyOptional key={}", key)
            read.findModelAggregateByKeyOptional(key)
        }
    }

    override fun findModelAggregateVersionOptional(modelId: ModelId, modelVersion: ModelVersion): ModelAggregate? {
        return db.withExposed {
            logger.debug("findModelAggregateVersionOptional id={} version={}", modelId, modelVersion)
            read.findModelAggregateVersionOptional(modelId, modelVersion)
        }

    }

    override fun findTypeByIdOptional(modelId: ModelId, typeId: TypeId): ModelType? {
        return db.withExposed {
            logger.debug("findTypeByIdOptional modelId={} typeId={}", modelId, typeId)
            read.findTypeByIdOptional(modelId, typeId)
        }
    }

    override fun findTypeByKeyOptional(modelId: ModelId, key: TypeKey): ModelType? {
        return db.withExposed {
            logger.debug("findTypeByKeyOptional modelId={} key={}", modelId, key)
            read.findTypeByKeyOptional(modelId, key)
        }
    }

    override fun findTypes(modelId: ModelId): List<ModelType> {
        return db.withExposed {
            logger.debug("findTypes modelId={}", modelId)
            read.findTypes(modelId)
        }
    }

    override fun findEntityByIdOptional(modelId: ModelId, entityId: EntityId): Entity? {
        return db.withExposed {
            logger.debug("findEntityByIdOptional modelId={} entityId={}", modelId, entityId)
            read.findEntityByIdOptional(modelId, entityId)
        }
    }

    override fun findEntityByKeyOptional(modelId: ModelId, entityKey: EntityKey): Entity? {
        return db.withExposed {
            logger.debug("findEntityByKeyOptional modelId={} entityKey={}", modelId, entityKey)
            read.findEntityByKeyOptional(modelId, entityKey)
        }
    }

    override fun findEntityPrimaryKeyOptional(modelId: ModelId, entityId: EntityId): EntityPrimaryKey? {
        return db.withExposed {
            logger.debug("findEntityPrimaryKeyOptional modelId={} entityId={}", modelId, entityId)
            read.findEntityPrimaryKeyOptional(modelId, entityId)
        }
    }

    override fun findBusinessKeyByIdOptional(modelId: ModelId, id: BusinessKeyId): BusinessKey? {
        return db.withExposed {
            logger.debug("findBusinessKeyByIdOptional modelId={} id={}", modelId, id)
            read.findBusinessKeyByIdOptional(modelId, id)
        }
    }

    override fun findBusinessKeyByKeyOptional(modelId: ModelId, key: BusinessKeyKey): BusinessKey? {
        return db.withExposed {
            logger.debug("findBusinessKeyByKeyOptional modelId={} key={}", modelId, key)
            read.findBusinessKeyByKeyOptional(modelId, key)
        }
    }

    override fun findBusinessKeys(modelId: ModelId): List<BusinessKey> {
        return db.withExposed {
            logger.debug("findBusinessKeys modelId={}", modelId)
            read.findBusinessKeys(modelId)
        }
    }

    override fun findEntityAttributeByIdOptional(
        modelId: ModelId,
        entityId: EntityId,
        attributeId: AttributeId
    ): Attribute? {
        return db.withExposed {
            logger.debug(
                "findEntityAttributeByIdOptional modelId={} entityId={} attributeId={}",
                modelId,
                entityId,
                attributeId
            )
            read.findEntityAttributeByIdOptional(modelId, entityId, attributeId)
        }
    }

    override fun findEntityAttributeByKeyOptional(modelId: ModelId, entityId: EntityId, key: AttributeKey): Attribute? {
        return db.withExposed {
            logger.debug("findEntityAttributeByKeyOptional modelId={} entityId={} key={}", modelId, entityId, key)
            read.findEntityAttributeByKeyOptional(modelId, entityId, key)
        }
    }

    override fun findRelationshipByIdOptional(modelId: ModelId, relationshipId: RelationshipId): Relationship? {
        return db.withExposed {
            logger.debug("findRelationshipByIdOptional modelId={} relationshipId={}", modelId, relationshipId)
            read.findRelationshipByIdOptional(modelId, relationshipId)
        }
    }

    override fun findRelationshipByKeyOptional(modelId: ModelId, relationshipKey: RelationshipKey): Relationship? {
        return db.withExposed {
            logger.debug("findRelationshipByKeyOptional modelId={} relationshipKey={}", modelId, relationshipKey)
            read.findRelationshipByKeyOptional(modelId, relationshipKey)
        }
    }

    override fun findRelationshipRoleByIdOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        roleId: RelationshipRoleId
    ): RelationshipRole? {
        return db.withExposed {
            logger.debug(
                "findRelationshipRoleByIdOptional modelId={} relationshipId={} roleId={}",
                modelId,
                relationshipId,
                roleId
            )
            read.findRelationshipRoleByIdOptional(modelId, relationshipId, roleId)
        }
    }

    override fun findRelationshipRoleByKeyOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        roleKey: RelationshipRoleKey
    ): RelationshipRole? {
        return db.withExposed {
            logger.debug(
                "findRelationshipRoleByKeyOptional modelId={} relationshipId={} roleKey={}",
                modelId,
                relationshipId,
                roleKey
            )
            read.findRelationshipRoleByKeyOptional(modelId, relationshipId, roleKey)
        }
    }

    override fun findRelationshipAttributeByIdOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        attributeId: AttributeId
    ): Attribute? {
        return db.withExposed {
            logger.debug(
                "findRelationshipAttributeByIdOptional modelId={} relationshipId={} attributeId={}",
                modelId,
                relationshipId,
                attributeId
            )
            read.findRelationshipAttributeByIdOptional(modelId, relationshipId, attributeId)
        }
    }

    override fun findRelationshipAttributeByKeyOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        key: AttributeKey
    ): Attribute? {
        return db.withExposed {
            logger.debug(
                "findRelationshipAttributeByKeyOptional modelId={} relationshipId={} key={}",
                modelId,
                relationshipId,
                key
            )
            read.findRelationshipAttributeByKeyOptional(modelId, relationshipId, key)
        }
    }

    override fun findDomainTagLocationsByTagId(tagId: TagId): List<DomainTagLocation> {
        return db.withExposed {
            logger.debug("findDomainTagLocationsByTagId tagId={}", tagId)
            read.findDomainTagLocationsByTagId(tagId)
        }
    }

    override fun isTypeUsedInEntityAttributes(modelId: ModelId, typeId: TypeId): Boolean {
        return db.withExposed {
            logger.debug("isTypeUsedInEntityAttributes modelId={} typeId={}", modelId, typeId)
            read.isTypeUsedInEntityAttributes(modelId, typeId)
        }
    }

    override fun isTypeUsedInRelationshipAttributes(modelId: ModelId, typeId: TypeId): Boolean {
        return db.withExposed {
            logger.debug("isTypeUsedInRelationshipAttributes modelId={} typeId={}", modelId, typeId)
            read.isTypeUsedInRelationshipAttributes(modelId, typeId)
        }
    }

    // -------------------------------------------------------------------------
    // History
    // -------------------------------------------------------------------------

    override fun findModelVersions(modelId: ModelId): List<ModelChangeEvent> {
        return db.withExposed {
            logger.debug("findModelVersions modelId={}", modelId)
            read.findModelVersions(modelId)
        }
    }

    override fun findAllModelChangeEvent(modelId: ModelId): List<ModelChangeEvent> {
        return db.withExposed {
            logger.debug("findAllModelChangeEvent modelId={}", modelId)
            read.findAllModelChangeEvent(modelId)
        }
    }

    override fun findModelChangeEventsInVersion(modelId: ModelId, version: ModelVersion): List<ModelChangeEvent> {
        return db.withExposed {
            logger.debug("findModelChangeEventsInVersion modelId={}", modelId)
            read.findModelChangeEventsInVersion(modelId, version)
        }
    }

    override fun findModelChangeEventsSinceLastReleaseEvent(modelId: ModelId): List<ModelChangeEvent> {
        return db.withExposed {
            logger.debug("findModelChangeEventsSinceLastReleaseEvent modelId={}", modelId)
            read.findModelChangeEventsSinceLastReleaseEvent(modelId)
        }
    }

    override fun findLastModelChangeEventOptional(modelId: ModelId): ModelChangeEvent? {
        return db.withExposed {
            logger.debug("findLastModelChangeEventOptional modelId={}", modelId)
            read.findLastModelChangeEventOptional(modelId)
        }
    }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    override fun search(query: ModelStorageSearchQuery): SearchResults {
        return db.withExposed {
            logger.debug("search query={}", query)
            searchRead.search(query)
        }
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    override fun dispatch(cmdEnv: ModelStorageCmdEnveloppe) {
        db.withExposed {
            logger.debug("dispatch cmd={}", cmdEnv.cmd)
            dispatchExposed(cmdEnv)
        }
    }

    override fun maintenanceRebuildCaches() {
        db.withExposed {
            logger.warn("Starting model projection cache rebuild from persisted events")
            ModelSnapshotTable.deleteAll()
            val eventRecords = ModelEventTable.selectAll()
                .orderBy(
                    ModelEventTable.modelId to SortOrder.ASC,
                    ModelEventTable.streamRevision to SortOrder.ASC
                )
                .map(ModelEventRecord::read)
            for (record in eventRecords) {
                logger.debug("processing event {}", record)
                processEvent(record.modelId, record) {
                    // do not store events
                }
            }
            logger.warn("Model projection cache rebuild completed. Replayed [{}] events.", eventRecords.size)
        }
    }

    private fun dispatchExposed(
        cmdEnv: ModelStorageCmdEnveloppe
    ) {
        val modelId = extractModelId(cmdEnv.cmd)
        val streamNumberCtx = eventSystem.eventStreamNumberManager.createNumberContext(modelId)
        val record = eventSystem.recordFactory.create(
            cmdEnv = cmdEnv,
            streamRevision = streamNumberCtx.nextRevision(),
            createdAt = clock.now()
        )
        processEvent(modelId, record) {
            appendModelEvent(streamNumberCtx, record)
        }
    }

    /**
     * Process a single event from the event source
     */
    private fun processEvent(modelId: ModelId, record: ModelEventRecord, storeEventIfNeeded: () -> Unit) {

        // Decode event in JSON into a real command
        val cmdAnyVersion: ModelStorageCmdAnyVersion = eventSystem.codec.decode(
            StorageEventEncoded(
                eventType = record.eventType,
                eventVersion = record.eventVersion,
                payload = record.payload
            )
        )

        // Old events may be translated into new events. An old event may be
        // converted into a succession of smaller events, so we may end up with
        // a list of events.
        val cmds: List<ModelStorageCmd> = eventSystem.upscale(cmdAnyVersion)

        // Treat each event independently, but only current versions
        for (cmd in cmds) {
            // Events are stored in a model, so we need to be sure the model
            // exists before doing anything. This happens when we see special
            // creation events like model_created or model_aggregate_stored.
            val modelSnapshotId = prepareStorageForAppend(cmd, modelId)

            // Event may be stored, or not. In a normal scenario when we receive
            // an event, we need to store it in the model. When we are just
            // replaying a stack of events, we won't.
            // It is the caller's responsibility to do that when storage is ready.
            storeEventIfNeeded()

            if (cmd is ModelStorageCmd.DeleteModel) {
                // Deleting a model is a special command because it removes the
                // model and all the event stack (complete destruction)
                deleteModel(cmd.modelId)
            } else {
                // Normal case, we maintain the current state (head snapshot)
                // and versions snapshot when we see model_released commands.
                // Note that this will incrementally manage the search engine
                // too.
                projection.projectCommand(
                    ProjectionEventCtx(
                        cmd = cmd,
                        modelId = record.modelId,
                        modelSnapshotId = modelSnapshotId,
                        modelEventId = record.id,
                        streamRevision = record.streamRevision
                    )
                )
                // Finally, we set in the head snapshot the current event
                // revision number we just processed and last update time.
                updateCurrentHeadProjectionMetadata(extractModelId(cmd), record.streamRevision)
            }
        }
    }


    /**
     * Keeps the projected CURRENT_HEAD metadata aligned with the latest event
     * revision that has been applied.
     */
    private fun updateCurrentHeadProjectionMetadata(modelId: ModelId, upToRevision: Int) {
        val now = clock.now()
        ModelSnapshotTable.update(where = { CurrentHeadByModelId(modelId).criterion() }) { row ->
            row[ModelSnapshotTable.upToRevision] = upToRevision
            row[ModelSnapshotTable.updatedAt] = now
        }
    }

    private fun extractModelId(cmd: ModelStorageCmd): ModelId {
        return when (cmd) {
            is ModelStorageCmd.CreateModel -> cmd.id
            is ModelStorageCmd.StoreModelAggregate -> cmd.model.id
            is ModelStorageCmdOnModel -> cmd.modelId
        }
    }

    private fun deleteModel(modelId: ModelId) {
        searchWrite.deleteModelBranch(modelId)
        ModelTable.deleteWhere { id eq modelId }
    }

    private fun appendModelEvent(
        streamNumberCtx: ModelEventStreamNumberContext,
        record: ModelEventRecord
    ) {
        try {
            ModelEventTable.insert { row ->
                row[ModelEventTable.id] = record.id
                row[ModelEventTable.modelId] = record.modelId
                row[ModelEventTable.streamRevision] = record.streamRevision
                row[ModelEventTable.eventType] = record.eventType
                row[ModelEventTable.eventVersion] = record.eventVersion
                row[ModelEventTable.modelVersion] = record.modelVersion
                row[ModelEventTable.actorId] = record.actorId
                row[ModelEventTable.traceabilityOrigin] = record.traceabilityOrigin
                row[ModelEventTable.createdAt] = record.createdAt
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

    }

    private fun prepareStorageForAppend(cmd: ModelStorageCmd, modelId: ModelId): ModelSnapshotId {
        return when (cmd) {
            is ModelStorageCmd.CreateModel -> {
                ensureModelIdentityExists(cmd.id)
                generateCurrentHeadModelSnapshotId()
            }

            is ModelStorageCmd.StoreModelAggregate -> {
                ensureModelIdentityExists(cmd.model.id)
                generateCurrentHeadModelSnapshotId()
            }

            else -> snapshotHead.toModelSnapshotId(modelId)
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


    private fun generateCurrentHeadModelSnapshotId(): ModelSnapshotId {
        return ModelSnapshotId.generate()
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ModelStorageDb::class.java)
    }


}
