package io.medatarun.tags.core.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.security.AppActorId
import io.medatarun.storage.eventsourcing.StorageEventEncoded
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.infra.db.events.TagEventSystem
import io.medatarun.tags.core.infra.db.records.TagEventRecord
import io.medatarun.tags.core.infra.db.tables.TagEventTable
import io.medatarun.tags.core.ports.needs.TagStorage
import io.medatarun.tags.core.ports.needs.TagStorageCmd
import io.medatarun.tags.core.ports.needs.TagStorageCmdEnveloppe
import io.medatarun.type.commons.id.Id
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.slf4j.LoggerFactory
import java.time.Instant.now

class TagStorageDb(private val dbConnectionFactory: DbConnectionFactory) : TagStorage {
    private class TagStorageDbEventNotFoundException(eventId: String) : MedatarunException("Tag event [$eventId] was appended but cannot be read back from storage.")
    private data class EventScope(
        val scopeType: String,
        val scopeId: String?
    )

    private val eventSystem = TagEventSystem()
    private val read = TagStorageDbRead()
    private val projection = TagStorageDbProjection()

    override fun findAllTag(): List<Tag> {
        return dbConnectionFactory.withExposed {
            read.findAllTag()
        }
    }

    override fun findTagByKeyOptional(scope: TagScopeRef, groupId: TagGroupId?, key: TagKey): Tag? {
        return dbConnectionFactory.withExposed {
            read.findTagByKeyOptional(scope, groupId, key)
        }
    }

    override fun findTagByIdOptional(id: TagId): Tag? {
        return dbConnectionFactory.withExposed {
            read.findTagByIdOptional(id)
        }
    }

    override fun findAllTagGroup(): List<TagGroup> {
        return dbConnectionFactory.withExposed {
            read.findAllTagGroup()
        }
    }

    override fun findTagGroupByIdOptional(id: TagGroupId): TagGroup? {
        return dbConnectionFactory.withExposed {
            read.findTagGroupByIdOptional(id)
        }
    }

    override fun findTagGroupByKeyOptional(key: TagGroupKey): TagGroup? {
        return dbConnectionFactory.withExposed {
            read.findTagGroupByKeyOptional(key)
        }
    }

    override fun dispatch(cmdEnv: TagStorageCmdEnveloppe) {
        logger.debug(cmdEnv.cmd.toString())
        dbConnectionFactory.withExposed {
            val cmd = cmdEnv.cmd
            if (cmd is TagStorageCmd.TagLocalScopeDelete) {
                deleteScope(cmd.scope)
            } else {
                val eventRecord = storeEvent(cmdEnv)
                val cmdFromEvent = decodeTagStorageCmd(eventRecord)
                projection.projectCommand(cmdFromEvent)
            }
        }
    }

    private fun deleteScope(scope: TagScopeRef) {
        if (scope is TagScopeRef.Global) {
            throw TagLocalScopeDeleteGlobalScopeException(scope.asString())
        }
        projection.projectCommand(TagStorageCmd.TagLocalScopeDelete(scope))
        when (scope) {
            is TagScopeRef.Local -> {
                val scopeId = scope.localScopeId.asString()
                TagEventTable.deleteWhere {
                    (TagEventTable.scopeType eq scope.type.value) and (TagEventTable.scopeId eq scopeId)
                }
            }
        }
    }

    private fun storeEvent(cmdEnv: TagStorageCmdEnveloppe): TagEventRecord {
        val cmd = cmdEnv.cmd
        val scope = cmd.scope
        val eventScope = toEventScope(scope)
        val streamRevisionCtx = eventSystem.eventStreamRevisionManager.createRevisionContext(
            scopeType = eventScope.scopeType,
            scopeId = eventScope.scopeId
        )
        val record = eventSystem.recordFactory.create(
            cmdEnv = cmdEnv,
            scopeType = eventScope.scopeType,
            scopeId = eventScope.scopeId,
            streamRevision = streamRevisionCtx.nextRevision(),
            createdAt = now()
        )
        try {
            TagEventTable.insert { row ->
                row[TagEventTable.id] = record.id
                row[TagEventTable.scopeType] = record.scopeType
                row[TagEventTable.scopeId] = record.scopeId
                row[TagEventTable.streamRevision] = record.streamRevision
                row[TagEventTable.eventType] = record.eventType
                row[TagEventTable.eventVersion] = record.eventVersion
                row[TagEventTable.actorId] = record.actorId.asString()
                row[TagEventTable.traceabilityOrigin] = record.traceabilityOrigin
                row[TagEventTable.createdAt] = record.createdAt.toString()
                row[TagEventTable.payload] = record.payload
            }
        } catch (e: Exception) {
            eventSystem.eventStreamRevisionManager.rethrowIfStreamRevisionConflict(
                exception = e,
                revisionContext = streamRevisionCtx,
                conflictingRevision = record.streamRevision
            )
            throw e
        }
        eventSystem.eventStreamRevisionManager.onAppendCommitted(streamRevisionCtx, record.streamRevision)
        return findEventById(record.id)
    }

    private fun toEventScope(scope: TagScopeRef): EventScope {
        return when (scope) {
            is TagScopeRef.Global -> EventScope(
                scopeType = TagScopeRef.Global.type.value,
                scopeId = null
            )

            is TagScopeRef.Local -> EventScope(
                scopeType = scope.type.value,
                scopeId = scope.localScopeId.asString()
            )
        }

    }

    private fun findEventById(eventId: String): TagEventRecord {
        return TagEventTable.selectAll().where { TagEventTable.id eq eventId }
            .singleOrNull()
            ?.let { eventRecordFromRow(it) }
            ?: throw TagStorageDbEventNotFoundException(eventId)
    }

    private fun eventRecordFromRow(row: ResultRow): TagEventRecord {
        return TagEventRecord(
            id = row[TagEventTable.id],
            scopeType = row[TagEventTable.scopeType],
            scopeId = row[TagEventTable.scopeId],
            streamRevision = row[TagEventTable.streamRevision],
            eventType = row[TagEventTable.eventType],
            eventVersion = row[TagEventTable.eventVersion],
            actorId = Id.fromString(row[TagEventTable.actorId], ::AppActorId),
            traceabilityOrigin = row[TagEventTable.traceabilityOrigin],
            createdAt = java.time.Instant.parse(row[TagEventTable.createdAt]),
            payload = row[TagEventTable.payload]
        )
    }

    private fun decodeTagStorageCmd(eventRecord: TagEventRecord): TagStorageCmd {
        return eventSystem.codec.decode(
            StorageEventEncoded(
                eventType = eventRecord.eventType,
                eventVersion = eventRecord.eventVersion,
                payload = eventRecord.payload
            )
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TagStorageDb::class.java)
    }

}
