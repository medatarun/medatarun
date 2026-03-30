package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.domain.search.SearchResults
import io.medatarun.model.infra.db.aggregate.ModelStorageDbAggregateReader
import io.medatarun.model.infra.db.events.ModelEventStreamNumberContext
import io.medatarun.model.infra.db.events.ModelEventSystem
import io.medatarun.model.infra.db.records.ModelEventRecord
import io.medatarun.model.infra.db.snapshots.ModelStorageDbProjection
import io.medatarun.model.infra.db.snapshots.ModelStorageDbProjection.ProjectionEventCtx
import io.medatarun.model.infra.db.snapshots.ModelStorageDbSnapshots
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
    private val snapshots = ModelStorageDbSnapshots()
    private val aggregateReader = ModelStorageDbAggregateReader()
    private val read = ModelStorageDbRead(eventSystem.registry, aggregateReader)
    private val projection = ModelStorageDbProjection(searchWrite, snapshots, clock)

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

    fun findAllModelEvents(modelId: ModelId): List<ModelEventRecord> {
        return db.withExposed {
            logger.debug("findAllModelEvents modelId={}", modelId)
            read.findAllModelEvents(modelId)
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
        processEvent(modelId,record) {
            appendModelEvent(streamNumberCtx, record)
        }
    }

    private fun processEvent(modelId: ModelId, record: ModelEventRecord, storeEventIfNeeded: () -> Unit) {

        val cmd = eventSystem.codec.decode(
            StorageEventEncoded(
                eventType = record.eventType,
                eventVersion = record.eventVersion,
                payload = record.payload
            )
        )

        val modelSnapshotId = prepareStorageForAppend(cmd, modelId)

        storeEventIfNeeded()


        if (cmd is ModelStorageCmd.DeleteModel) {
            deleteModel(cmd.modelId)
        } else {

            projection.projectCommand(
                ProjectionEventCtx(
                    cmd = cmd,
                    modelId = record.modelId,
                    modelSnapshotId = modelSnapshotId,
                    modelEventId = record.id,
                    streamRevision = record.streamRevision
                )
            )
            updateCurrentHeadProjectionMetadata(extractModelId(cmd), record.streamRevision)
        }

    }


    /**
     * Keeps the projected CURRENT_HEAD metadata aligned with the latest event
     * revision that has been applied.
     */
    private fun updateCurrentHeadProjectionMetadata(modelId: ModelId, upToRevision: Int) {
        val now = clock.now().toString()
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

            else -> snapshots.currentHeadModelSnapshotId(modelId)
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
