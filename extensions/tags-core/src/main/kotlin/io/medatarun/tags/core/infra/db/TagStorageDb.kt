package io.medatarun.tags.core.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.storage.eventsourcing.StorageEventEncoded
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.infra.db.events.TagEventSystem
import io.medatarun.tags.core.infra.db.records.TagEventRecord
import io.medatarun.tags.core.infra.db.tables.TagEventTable
import io.medatarun.tags.core.ports.needs.TagStorage
import io.medatarun.tags.core.ports.needs.TagStorageCmd
import io.medatarun.tags.core.ports.needs.TagStorageCmdEnveloppe
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.Instant.now

class TagStorageDb(private val dbConnectionFactory: DbConnectionFactory) : TagStorage {
    private class TagStorageDbEventNotFoundException(eventId: String) : MedatarunException("Tag event [$eventId] was appended but cannot be read back from storage.")
    private data class EventScope(
        val scopeType: String,
        val scopeId: TagScopeId?
    )

    private val eventSystem = TagEventSystem()
    private val read = TagStorageDbRead()
    private val projection = TagStorageDbProjection()
    private val historyProjection = TagStorageDbHistoryProjection()

    override fun findAllTag(): List<Tag> {
        return dbConnectionFactory.withExposed {
            read.findAllTag()
        }
    }

    override fun findAllTagByScopeRef(scopeRef: TagScopeRef): List<Tag> {
        return dbConnectionFactory.withExposed {
            read.findAllTagByScopeRef(scopeRef)
        }
    }

    override fun search(query: TagSearchFilters): List<Tag> {
        return dbConnectionFactory.withExposed {
            read.search(query)
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

    override fun findTagByIdAsOfOptional(id: TagId, eventDate: Instant): Tag? {
        return dbConnectionFactory.withExposed {
            read.findTagByIdAsOfOptional(id, eventDate)
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

    override fun findTagGroupByIdAsOfOptional(id: TagGroupId, eventDate: Instant): TagGroup? {
        return dbConnectionFactory.withExposed {
            read.findTagGroupByIdAsOfOptional(id, eventDate)
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
                historyProjection.projectCommandFromEvent(cmdFromEvent, eventRecord.id, eventRecord.createdAt)
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
                TagEventTable.deleteWhere {
                    (TagEventTable.scopeType eq scope.type.value) and (TagEventTable.scopeId eq scope.localScopeId)
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
                row[TagEventTable.actorId] = record.actorId
                row[TagEventTable.traceabilityOrigin] = record.traceabilityOrigin
                row[TagEventTable.createdAt] = record.createdAt
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
                scopeId = scope.localScopeId
            )
        }

    }

    private fun findEventById(eventId: TagEventId): TagEventRecord {
        return TagEventTable.selectAll().where { TagEventTable.id eq eventId }
            .singleOrNull()
            ?.let { eventRecordFromRow(it) }
            ?: throw TagStorageDbEventNotFoundException(eventId.asString())
    }

    private fun eventRecordFromRow(row: ResultRow): TagEventRecord {
        return TagEventRecord(
            id = row[TagEventTable.id],
            scopeType = row[TagEventTable.scopeType],
            scopeId = row[TagEventTable.scopeId],
            streamRevision = row[TagEventTable.streamRevision],
            eventType = row[TagEventTable.eventType],
            eventVersion = row[TagEventTable.eventVersion],
            actorId = row[TagEventTable.actorId],
            traceabilityOrigin = row[TagEventTable.traceabilityOrigin],
            createdAt = row[TagEventTable.createdAt],
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
