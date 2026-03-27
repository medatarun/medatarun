package io.medatarun.tags.core.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.infra.db.tables.TagGroupHistoryProjectionTable
import io.medatarun.tags.core.infra.db.tables.TagGroupProjectionTable
import io.medatarun.tags.core.infra.db.tables.TagHistoryProjectionTable
import io.medatarun.tags.core.infra.db.tables.TagProjectionTable
import io.medatarun.tags.core.ports.needs.TagStorageCmd
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.util.UUID

internal class TagStorageDbHistoryProjection {
    private class TagStorageDbHistoryProjectionTagRowMissingException(tagId: String) :
        MedatarunException("Tag projection row [$tagId] was expected but not found while writing tag history projection.")

    private class TagStorageDbHistoryProjectionTagGroupRowMissingException(tagGroupId: String) :
        MedatarunException("Tag group projection row [$tagGroupId] was expected but not found while writing tag history projection.")

    fun projectCommandFromEvent(cmd: TagStorageCmd, tagEventId: String, eventCreatedAt: Instant) {
        val scope = cmd.scope
        val eventCtx = TagHistoryEventContext(tagEventId, eventCreatedAt)
        when (cmd) {
            is TagStorageCmd.TagCreate -> {
                closeActiveTagHistory(cmd.tagId.asString(), eventCtx.eventCreatedAt)
                TagHistoryProjectionTable.insert { row ->
                    row[TagHistoryProjectionTable.id] = nextHistoryRowId()
                    row[TagHistoryProjectionTable.tagEventId] = eventCtx.tagEventId
                    row[TagHistoryProjectionTable.tagId] = cmd.tagId.asString()
                    row[TagHistoryProjectionTable.scopeType] = cmd.scope.type.value
                    when (scope) {
                        is TagScopeRef.Global -> row[TagHistoryProjectionTable.scopeId] = null
                        is TagScopeRef.Local -> row[TagHistoryProjectionTable.scopeId] = scope.localScopeId.asString()
                    }
                    row[TagHistoryProjectionTable.tagGroupId] = cmd.groupId?.asString()
                    row[TagHistoryProjectionTable.key] = cmd.key.asString()
                    row[TagHistoryProjectionTable.name] = cmd.name
                    row[TagHistoryProjectionTable.description] = cmd.description
                    row[TagHistoryProjectionTable.validFrom] = instantAsDbText(eventCtx.eventCreatedAt)
                    row[TagHistoryProjectionTable.validTo] = null
                }
            }

            is TagStorageCmd.TagUpdateKey -> {
                val tagRow = loadTagProjectionRow(cmd.tagId.asString())
                closeActiveTagHistory(cmd.tagId.asString(), eventCtx.eventCreatedAt)
                insertTagHistoryRow(tagRow, eventCtx)
            }

            is TagStorageCmd.TagUpdateName -> {
                val tagRow = loadTagProjectionRow(cmd.tagId.asString())
                closeActiveTagHistory(cmd.tagId.asString(), eventCtx.eventCreatedAt)
                insertTagHistoryRow(tagRow, eventCtx)
            }

            is TagStorageCmd.TagUpdateDescription -> {
                val tagRow = loadTagProjectionRow(cmd.tagId.asString())
                closeActiveTagHistory(cmd.tagId.asString(), eventCtx.eventCreatedAt)
                insertTagHistoryRow(tagRow, eventCtx)
            }

            is TagStorageCmd.TagDelete -> {
                closeActiveTagHistory(cmd.tagId.asString(), eventCtx.eventCreatedAt)
            }

            is TagStorageCmd.TagGroupCreate -> {
                closeActiveTagGroupHistory(cmd.tagGroupId.asString(), eventCtx.eventCreatedAt)
                TagGroupHistoryProjectionTable.insert { row ->
                    row[TagGroupHistoryProjectionTable.id] = nextHistoryRowId()
                    row[TagGroupHistoryProjectionTable.tagEventId] = eventCtx.tagEventId
                    row[TagGroupHistoryProjectionTable.tagGroupId] = cmd.tagGroupId.asString()
                    row[TagGroupHistoryProjectionTable.key] = cmd.key.asString()
                    row[TagGroupHistoryProjectionTable.name] = cmd.name
                    row[TagGroupHistoryProjectionTable.description] = cmd.description
                    row[TagGroupHistoryProjectionTable.validFrom] = instantAsDbText(eventCtx.eventCreatedAt)
                    row[TagGroupHistoryProjectionTable.validTo] = null
                }
            }

            is TagStorageCmd.TagGroupUpdateKey -> {
                val tagGroupRow = loadTagGroupProjectionRow(cmd.tagGroupId.asString())
                closeActiveTagGroupHistory(cmd.tagGroupId.asString(), eventCtx.eventCreatedAt)
                insertTagGroupHistoryRow(tagGroupRow, eventCtx)
            }

            is TagStorageCmd.TagGroupUpdateName -> {
                val tagGroupRow = loadTagGroupProjectionRow(cmd.tagGroupId.asString())
                closeActiveTagGroupHistory(cmd.tagGroupId.asString(), eventCtx.eventCreatedAt)
                insertTagGroupHistoryRow(tagGroupRow, eventCtx)
            }

            is TagStorageCmd.TagGroupUpdateDescription -> {
                val tagGroupRow = loadTagGroupProjectionRow(cmd.tagGroupId.asString())
                closeActiveTagGroupHistory(cmd.tagGroupId.asString(), eventCtx.eventCreatedAt)
                insertTagGroupHistoryRow(tagGroupRow, eventCtx)
            }

            is TagStorageCmd.TagGroupDelete -> {
                closeActiveTagGroupHistory(cmd.tagGroupId.asString(), eventCtx.eventCreatedAt)
            }

            is TagStorageCmd.TagLocalScopeDelete -> {
                // No tag event is produced for scope delete. History rows are removed
                // by ON DELETE CASCADE when tag_event rows are purged from this scope.
            }
        }
    }

    private fun loadTagProjectionRow(tagId: String): ResultRow {
        return TagProjectionTable.selectAll().where { TagProjectionTable.id eq tagId }.singleOrNull()
            ?: throw TagStorageDbHistoryProjectionTagRowMissingException(tagId)
    }

    private fun loadTagGroupProjectionRow(tagGroupId: String): ResultRow {
        return TagGroupProjectionTable.selectAll().where { TagGroupProjectionTable.id eq tagGroupId }.singleOrNull()
            ?: throw TagStorageDbHistoryProjectionTagGroupRowMissingException(tagGroupId)
    }

    private fun closeActiveTagHistory(tagId: String, eventCreatedAt: Instant) {
        // Interval contract: [valid_from, valid_to). valid_to stays null while the version is active.
        TagHistoryProjectionTable.update(
            where = {
                (TagHistoryProjectionTable.tagId eq tagId) and TagHistoryProjectionTable.validTo.isNull()
            }
        ) { row ->
            row[TagHistoryProjectionTable.validTo] = instantAsDbText(eventCreatedAt)
        }
    }

    private fun closeActiveTagGroupHistory(tagGroupId: String, eventCreatedAt: Instant) {
        // Interval contract: [valid_from, valid_to). valid_to stays null while the version is active.
        TagGroupHistoryProjectionTable.update(
            where = {
                (TagGroupHistoryProjectionTable.tagGroupId eq tagGroupId) and TagGroupHistoryProjectionTable.validTo.isNull()
            }
        ) { row ->
            row[TagGroupHistoryProjectionTable.validTo] = instantAsDbText(eventCreatedAt)
        }
    }

    private fun insertTagHistoryRow(tagRow: ResultRow, eventCtx: TagHistoryEventContext) {
        TagHistoryProjectionTable.insert { row ->
            row[TagHistoryProjectionTable.id] = nextHistoryRowId()
            row[TagHistoryProjectionTable.tagEventId] = eventCtx.tagEventId
            row[TagHistoryProjectionTable.tagId] = tagRow[TagProjectionTable.id]
            row[TagHistoryProjectionTable.scopeType] = tagRow[TagProjectionTable.scopeType]
            row[TagHistoryProjectionTable.scopeId] = tagRow[TagProjectionTable.scopeId]
            row[TagHistoryProjectionTable.tagGroupId] = tagRow[TagProjectionTable.tagGroupId]
            row[TagHistoryProjectionTable.key] = tagRow[TagProjectionTable.key]
            row[TagHistoryProjectionTable.name] = tagRow[TagProjectionTable.name]
            row[TagHistoryProjectionTable.description] = tagRow[TagProjectionTable.description]
            row[TagHistoryProjectionTable.validFrom] = instantAsDbText(eventCtx.eventCreatedAt)
            row[TagHistoryProjectionTable.validTo] = null
        }
    }

    private fun insertTagGroupHistoryRow(tagGroupRow: ResultRow, eventCtx: TagHistoryEventContext) {
        TagGroupHistoryProjectionTable.insert { row ->
            row[TagGroupHistoryProjectionTable.id] = nextHistoryRowId()
            row[TagGroupHistoryProjectionTable.tagEventId] = eventCtx.tagEventId
            row[TagGroupHistoryProjectionTable.tagGroupId] = tagGroupRow[TagGroupProjectionTable.id]
            row[TagGroupHistoryProjectionTable.key] = tagGroupRow[TagGroupProjectionTable.key]
            row[TagGroupHistoryProjectionTable.name] = tagGroupRow[TagGroupProjectionTable.name]
            row[TagGroupHistoryProjectionTable.description] = tagGroupRow[TagGroupProjectionTable.description]
            row[TagGroupHistoryProjectionTable.validFrom] = instantAsDbText(eventCtx.eventCreatedAt)
            row[TagGroupHistoryProjectionTable.validTo] = null
        }
    }

    private fun instantAsDbText(value: Instant): String {
        return value.toString()
    }

    private fun nextHistoryRowId(): String {
        return UUID.randomUUID().toString()
    }

    private data class TagHistoryEventContext(
        val tagEventId: String,
        val eventCreatedAt: Instant
    )
}
