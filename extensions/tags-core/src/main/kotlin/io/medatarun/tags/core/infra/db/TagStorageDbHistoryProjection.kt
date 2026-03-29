package io.medatarun.tags.core.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.tags.core.domain.TagEventId
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.infra.db.tables.TagViewHistory_TagGroup_Table
import io.medatarun.tags.core.infra.db.tables.TagViewCurrent_TagGroup_Table
import io.medatarun.tags.core.infra.db.tables.TagViewHistory_Tag_Table
import io.medatarun.tags.core.infra.db.tables.TagViewCurrent_Tag_Table
import io.medatarun.tags.core.infra.db.types.TagGroupHistoryProjectionId
import io.medatarun.tags.core.infra.db.types.TagHistoryProjectionId
import io.medatarun.tags.core.ports.needs.TagStorageCmd
import io.medatarun.type.commons.id.Id
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant

internal class TagStorageDbHistoryProjection {
    private class TagStorageDbHistoryProjectionTagRowMissingException(tagId: TagId) :
        MedatarunException("Tag projection row [${tagId.asString()}] was expected but not found while writing tag history projection.")

    private class TagStorageDbHistoryProjectionTagGroupRowMissingException(tagGroupId: TagGroupId) :
        MedatarunException("Tag group projection row [${tagGroupId.asString()}] was expected but not found while writing tag history projection.")

    fun projectCommandFromEvent(cmd: TagStorageCmd, tagEventId: TagEventId, eventCreatedAt: Instant) {
        val scope = cmd.scope
        val eventCtx = TagHistoryEventContext(tagEventId, eventCreatedAt)
        when (cmd) {
            is TagStorageCmd.TagCreate -> {
                closeActiveTagHistory(cmd.tagId, eventCtx.eventCreatedAt)
                TagViewHistory_Tag_Table.insert { row ->
                    row[TagViewHistory_Tag_Table.id] = Id.generate(::TagHistoryProjectionId)
                    row[TagViewHistory_Tag_Table.tagEventId] = eventCtx.tagEventId
                    row[TagViewHistory_Tag_Table.tagId] = cmd.tagId
                    row[TagViewHistory_Tag_Table.scopeType] = cmd.scope.type.value
                    when (scope) {
                        is TagScopeRef.Global -> row[TagViewHistory_Tag_Table.scopeId] = null
                        is TagScopeRef.Local -> row[TagViewHistory_Tag_Table.scopeId] = scope.localScopeId
                    }
                    row[TagViewHistory_Tag_Table.tagGroupId] = cmd.groupId
                    row[TagViewHistory_Tag_Table.key] = cmd.key
                    row[TagViewHistory_Tag_Table.name] = cmd.name
                    row[TagViewHistory_Tag_Table.description] = cmd.description
                    row[TagViewHistory_Tag_Table.validFrom] = eventCtx.eventCreatedAt
                    row[TagViewHistory_Tag_Table.validTo] = null
                }
            }

            is TagStorageCmd.TagUpdateKey -> {
                val tagRow = loadTagProjectionRow(cmd.tagId)
                closeActiveTagHistory(cmd.tagId, eventCtx.eventCreatedAt)
                insertTagHistoryRow(tagRow, eventCtx)
            }

            is TagStorageCmd.TagUpdateName -> {
                val tagRow = loadTagProjectionRow(cmd.tagId)
                closeActiveTagHistory(cmd.tagId, eventCtx.eventCreatedAt)
                insertTagHistoryRow(tagRow, eventCtx)
            }

            is TagStorageCmd.TagUpdateDescription -> {
                val tagRow = loadTagProjectionRow(cmd.tagId)
                closeActiveTagHistory(cmd.tagId, eventCtx.eventCreatedAt)
                insertTagHistoryRow(tagRow, eventCtx)
            }

            is TagStorageCmd.TagDelete -> {
                closeActiveTagHistory(cmd.tagId, eventCtx.eventCreatedAt)
            }

            is TagStorageCmd.TagGroupCreate -> {
                closeActiveTagGroupHistory(cmd.tagGroupId, eventCtx.eventCreatedAt)
                TagViewHistory_TagGroup_Table.insert { row ->
                    row[TagViewHistory_TagGroup_Table.id] = Id.generate(::TagGroupHistoryProjectionId)
                    row[TagViewHistory_TagGroup_Table.tagEventId] = eventCtx.tagEventId
                    row[TagViewHistory_TagGroup_Table.tagGroupId] = cmd.tagGroupId
                    row[TagViewHistory_TagGroup_Table.key] = cmd.key
                    row[TagViewHistory_TagGroup_Table.name] = cmd.name
                    row[TagViewHistory_TagGroup_Table.description] = cmd.description
                    row[TagViewHistory_TagGroup_Table.validFrom] = eventCtx.eventCreatedAt
                    row[TagViewHistory_TagGroup_Table.validTo] = null
                }
            }

            is TagStorageCmd.TagGroupUpdateKey -> {
                val tagGroupRow = loadTagGroupProjectionRow(cmd.tagGroupId)
                closeActiveTagGroupHistory(cmd.tagGroupId, eventCtx.eventCreatedAt)
                insertTagGroupHistoryRow(tagGroupRow, eventCtx)
            }

            is TagStorageCmd.TagGroupUpdateName -> {
                val tagGroupRow = loadTagGroupProjectionRow(cmd.tagGroupId)
                closeActiveTagGroupHistory(cmd.tagGroupId, eventCtx.eventCreatedAt)
                insertTagGroupHistoryRow(tagGroupRow, eventCtx)
            }

            is TagStorageCmd.TagGroupUpdateDescription -> {
                val tagGroupRow = loadTagGroupProjectionRow(cmd.tagGroupId)
                closeActiveTagGroupHistory(cmd.tagGroupId, eventCtx.eventCreatedAt)
                insertTagGroupHistoryRow(tagGroupRow, eventCtx)
            }

            is TagStorageCmd.TagGroupDelete -> {
                closeActiveTagGroupHistory(cmd.tagGroupId, eventCtx.eventCreatedAt)
            }

            is TagStorageCmd.TagLocalScopeDelete -> {
                // No tag event is produced for scope delete. History rows are removed
                // by ON DELETE CASCADE when tag_event rows are purged from this scope.
            }
        }
    }

    private fun loadTagProjectionRow(tagId: TagId): ResultRow {
        return TagViewCurrent_Tag_Table.selectAll().where { TagViewCurrent_Tag_Table.id eq tagId }.singleOrNull()
            ?: throw TagStorageDbHistoryProjectionTagRowMissingException(tagId)
    }

    private fun loadTagGroupProjectionRow(tagGroupId: TagGroupId): ResultRow {
        return TagViewCurrent_TagGroup_Table.selectAll().where { TagViewCurrent_TagGroup_Table.id eq tagGroupId }.singleOrNull()
            ?: throw TagStorageDbHistoryProjectionTagGroupRowMissingException(tagGroupId)
    }

    private fun closeActiveTagHistory(tagId: TagId, eventCreatedAt: Instant) {
        // Interval contract: [valid_from, valid_to). valid_to stays null while the version is active.
        TagViewHistory_Tag_Table.update(
            where = {
                (TagViewHistory_Tag_Table.tagId eq tagId) and TagViewHistory_Tag_Table.validTo.isNull()
            }
        ) { row ->
            row[TagViewHistory_Tag_Table.validTo] = eventCreatedAt
        }
    }

    private fun closeActiveTagGroupHistory(tagGroupId: TagGroupId, eventCreatedAt: Instant) {
        // Interval contract: [valid_from, valid_to). valid_to stays null while the version is active.
        TagViewHistory_TagGroup_Table.update(
            where = {
                (TagViewHistory_TagGroup_Table.tagGroupId eq tagGroupId) and TagViewHistory_TagGroup_Table.validTo.isNull()
            }
        ) { row ->
            row[TagViewHistory_TagGroup_Table.validTo] = eventCreatedAt
        }
    }

    private fun insertTagHistoryRow(tagRow: ResultRow, eventCtx: TagHistoryEventContext) {
        TagViewHistory_Tag_Table.insert { row ->
            row[TagViewHistory_Tag_Table.id] = Id.generate(::TagHistoryProjectionId)
            row[TagViewHistory_Tag_Table.tagEventId] = eventCtx.tagEventId
            row[TagViewHistory_Tag_Table.tagId] = tagRow[TagViewCurrent_Tag_Table.id]
            row[TagViewHistory_Tag_Table.scopeType] = tagRow[TagViewCurrent_Tag_Table.scopeType]
            row[TagViewHistory_Tag_Table.scopeId] = tagRow[TagViewCurrent_Tag_Table.scopeId]
            row[TagViewHistory_Tag_Table.tagGroupId] = tagRow[TagViewCurrent_Tag_Table.tagGroupId]
            row[TagViewHistory_Tag_Table.key] = tagRow[TagViewCurrent_Tag_Table.key]
            row[TagViewHistory_Tag_Table.name] = tagRow[TagViewCurrent_Tag_Table.name]
            row[TagViewHistory_Tag_Table.description] = tagRow[TagViewCurrent_Tag_Table.description]
            row[TagViewHistory_Tag_Table.validFrom] = eventCtx.eventCreatedAt
            row[TagViewHistory_Tag_Table.validTo] = null
        }
    }

    private fun insertTagGroupHistoryRow(tagGroupRow: ResultRow, eventCtx: TagHistoryEventContext) {
        TagViewHistory_TagGroup_Table.insert { row ->
            row[TagViewHistory_TagGroup_Table.id] = Id.generate(::TagGroupHistoryProjectionId)
            row[TagViewHistory_TagGroup_Table.tagEventId] = eventCtx.tagEventId
            row[TagViewHistory_TagGroup_Table.tagGroupId] = tagGroupRow[TagViewCurrent_TagGroup_Table.id]
            row[TagViewHistory_TagGroup_Table.key] = tagGroupRow[TagViewCurrent_TagGroup_Table.key]
            row[TagViewHistory_TagGroup_Table.name] = tagGroupRow[TagViewCurrent_TagGroup_Table.name]
            row[TagViewHistory_TagGroup_Table.description] = tagGroupRow[TagViewCurrent_TagGroup_Table.description]
            row[TagViewHistory_TagGroup_Table.validFrom] = eventCtx.eventCreatedAt
            row[TagViewHistory_TagGroup_Table.validTo] = null
        }
    }

    private data class TagHistoryEventContext(
        val tagEventId: TagEventId,
        val eventCreatedAt: Instant
    )
}
