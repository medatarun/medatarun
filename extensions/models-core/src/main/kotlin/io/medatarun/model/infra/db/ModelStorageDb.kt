package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.domain.search.SearchResults
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
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
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
    private val read = ModelStorageDbRead(snapshots, eventSystem.registry)
    private val projection = ModelStorageDbProjection(searchWrite, snapshots, clock)

    override fun existsModelById(id: ModelId): Boolean = db.withExposed { read.existsModelById(id) }
    override fun existsModelByKey(key: ModelKey): Boolean = db.withExposed { read.existsModelByKey(key) }
    override fun findAllModelIds(): List<ModelId> = db.withExposed { read.findAllModelIds() }
    override fun findModelByKeyOptional(key: ModelKey): Model? = db.withExposed { read.findModelByKeyOptional(key) }
    override fun findModelByIdOptional(id: ModelId): Model? = db.withExposed { read.findModelByIdOptional(id) }
    override fun findLatestModelReleaseVersionOptional(modelId: ModelId): ModelVersion? = db.withExposed { read.findLatestModelReleaseVersionOptional(modelId) }
    override fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate? = db.withExposed { read.findModelAggregateByIdOptional(id) }
    override fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate? = db.withExposed { read.findModelAggregateByKeyOptional(key) }
    override fun findTypeByIdOptional(modelId: ModelId, typeId: TypeId): ModelType?  = db.withExposed { read.findTypeByIdOptional(modelId, typeId) }
    override fun findTypeByKeyOptional(modelId: ModelId, key: TypeKey): ModelType? = db.withExposed { read.findTypeByKeyOptional(modelId, key) }
    override fun findEntityByIdOptional(modelId: ModelId, entityId: EntityId): Entity?  = db.withExposed { read.findEntityByIdOptional(modelId, entityId) }
    override fun findEntityByKeyOptional(modelId: ModelId, entityKey: EntityKey): Entity? = db.withExposed { read.findEntityByKeyOptional(modelId, entityKey) }
    override fun findEntityAttributeByIdOptional(modelId: ModelId, entityId: EntityId, attributeId: AttributeId): Attribute?  = db.withExposed { read.findEntityAttributeByIdOptional(modelId, entityId, attributeId) }
    override fun findEntityAttributeByKeyOptional(modelId: ModelId, entityId: EntityId, key: AttributeKey): Attribute? = db.withExposed { read.findEntityAttributeByKeyOptional(modelId, entityId, key) }
    override fun findRelationshipByIdOptional(modelId: ModelId, relationshipId: RelationshipId): Relationship? = db.withExposed { read.findRelationshipByIdOptional(modelId, relationshipId) }
    override fun findRelationshipByKeyOptional(modelId: ModelId, relationshipKey: RelationshipKey): Relationship? = db.withExposed { read.findRelationshipByKeyOptional(modelId, relationshipKey) }
    override fun findRelationshipRoleByIdOptional(modelId: ModelId, relationshipId: RelationshipId, roleId: RelationshipRoleId): RelationshipRole? = db.withExposed { read.findRelationshipRoleByIdOptional(modelId, relationshipId, roleId) }
    override fun findRelationshipRoleByKeyOptional(modelId: ModelId, relationshipId: RelationshipId, roleKey: RelationshipRoleKey): RelationshipRole? = db.withExposed { read.findRelationshipRoleByKeyOptional(modelId, relationshipId, roleKey) }
    override fun findRelationshipAttributeByIdOptional(modelId: ModelId, relationshipId: RelationshipId, attributeId: AttributeId): Attribute? = db.withExposed { read.findRelationshipAttributeByIdOptional(modelId, relationshipId, attributeId) }
    override fun findRelationshipAttributeByKeyOptional(modelId: ModelId, relationshipId: RelationshipId, key: AttributeKey): Attribute?  = db.withExposed { read.findRelationshipAttributeByKeyOptional(modelId, relationshipId, key) }
    override fun isTypeUsedInEntityAttributes(modelId: ModelId, typeId: TypeId): Boolean  = db.withExposed { read.isTypeUsedInEntityAttributes(modelId, typeId) }
    override fun isTypeUsedInRelationshipAttributes(modelId: ModelId, typeId: TypeId): Boolean  = db.withExposed { read.isTypeUsedInRelationshipAttributes(modelId, typeId) }

    fun findAllModelEvents(modelId: ModelId) = db.withExposed { read.findAllModelEvents(modelId) }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    override fun search(query: ModelStorageSearchQuery): SearchResults {
        return db.withExposed {
            searchRead.search(query)
        }
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    override fun dispatch(cmdEnv: ModelStorageCmdEnveloppe) {
        db.withExposed {
            dispatchExposed(cmdEnv)
        }
    }

    private fun dispatchExposed(
        cmdEnv: ModelStorageCmdEnveloppe
    ) {
        val cmd = cmdEnv.cmd
        if (cmd is ModelStorageCmd.DeleteModel) {
            deleteModel(cmd.modelId)
        } else {
            val modelId = extractModelId(cmdEnv.cmd)
            val streamNumberCtx = eventSystem.eventStreamNumberManager.createNumberContext(modelId)
            val modelSnapshotId = prepareStorageForAppend(cmdEnv.cmd, modelId)
            val record = appendModelEvent(cmdEnv, streamNumberCtx)
            projectModelEvent(record, modelSnapshotId)
            updateCurrentHeadProjectionMetadata(extractModelId(cmd), record.streamRevision)
        }
    }



    fun projectModelEvent(record: ModelEventRecord, modelSnapshotId: ModelSnapshotId) {
        val cmd = eventSystem.codec.decode(
            io.medatarun.model.infra.db.events.ModelEventEncoded(
                eventType = record.eventType,
                eventVersion = record.eventVersion,
                payload = record.payload
            )
        )
        projection.projectCommand(
            ProjectionEventCtx(
                cmd = cmd,
                modelId = record.modelId,
                modelSnapshotId = modelSnapshotId,
                modelEventId = record.id,
                streamRevision = record.streamRevision
            )
        )
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
