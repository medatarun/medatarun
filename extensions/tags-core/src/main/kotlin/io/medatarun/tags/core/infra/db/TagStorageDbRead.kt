package io.medatarun.tags.core.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.tags.core.domain.Tag
import io.medatarun.tags.core.domain.TagGroup
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.tags.core.infra.db.tables.TagGroupTable
import io.medatarun.tags.core.infra.db.tables.TagTable
import io.medatarun.tags.core.internal.TagGroupInMemory
import io.medatarun.tags.core.internal.TagInMemory
import io.medatarun.type.commons.id.Id
import io.medatarun.type.commons.key.Key
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.selectAll

internal class TagStorageDbRead {
    private class TagStorageDbInvalidGlobalLookupException : MedatarunException("Global tag lookup requires groupId")

    fun findAllTag(): List<Tag> {
        return TagTable.selectAll().map { row -> tagFromRow(row) }
    }

    fun findTagByKeyOptional(scope: TagScopeRef, groupId: TagGroupId?, key: TagKey): Tag? {
        return when (scope) {
            is TagScopeRef.Local -> {
                TagTable.selectAll().where {
                    (TagTable.scopeType eq scope.type.value) and
                        (TagTable.scopeId eq scope.localScopeId.asString()) and
                        TagTable.tagGroupId.isNull() and
                        (TagTable.key eq key.value)
                }.singleOrNull()?.let { row -> tagFromRow(row) }
            }

            is TagScopeRef.Global -> {
                val effectiveGroupId = groupId ?: throw TagStorageDbInvalidGlobalLookupException()
                TagTable.selectAll().where {
                    (TagTable.scopeType eq scope.type.value) and
                        TagTable.scopeId.isNull() and
                        (TagTable.tagGroupId eq effectiveGroupId.asString()) and
                        (TagTable.key eq key.value)
                }.singleOrNull()?.let { row -> tagFromRow(row) }
            }
        }
    }

    fun findTagByIdOptional(id: TagId): Tag? {
        return TagTable.selectAll().where { TagTable.id eq id.asString() }
            .singleOrNull()
            ?.let { row -> tagFromRow(row) }
    }

    fun findAllTagGroup(): List<TagGroup> {
        return TagGroupTable.selectAll().map { row -> tagGroupFromRow(row) }
    }

    fun findTagGroupByIdOptional(id: TagGroupId): TagGroup? {
        return TagGroupTable.selectAll().where { TagGroupTable.id eq id.asString() }
            .singleOrNull()
            ?.let { row -> tagGroupFromRow(row) }
    }

    fun findTagGroupByKeyOptional(key: TagGroupKey): TagGroup? {
        return TagGroupTable.selectAll().where { TagGroupTable.key eq key.value }
            .singleOrNull()
            ?.let { row -> tagGroupFromRow(row) }
    }

    private fun tagGroupFromRow(row: ResultRow): TagGroup {
        return TagGroupInMemory(
            id = Id.fromString(row[TagGroupTable.id], ::TagGroupId),
            key = Key.fromString(row[TagGroupTable.key], ::TagGroupKey),
            name = row[TagGroupTable.name],
            description = row[TagGroupTable.description]
        )
    }

    private fun tagFromRow(row: ResultRow): Tag {
        val scopeType = TagScopeType(row[TagTable.scopeType])
        val scopeIdString = row[TagTable.scopeId]
        val scope = if (scopeType.value == TagScopeRef.Global.type.value) {
            TagScopeRef.Global
        } else {
            val localScopeId = requireNotNull(scopeIdString) {
                "Local tag row missing scope_id"
            }
            TagScopeRef.Local(scopeType, Id.fromString(localScopeId, ::TagScopeId))
        }
        val groupIdString = row[TagTable.tagGroupId]
        val groupId = if (groupIdString == null) null else Id.fromString(groupIdString, ::TagGroupId)
        return TagInMemory(
            id = Id.fromString(row[TagTable.id], ::TagId),
            scope = scope,
            groupId = groupId,
            key = Key.fromString(row[TagTable.key], ::TagKey),
            name = row[TagTable.name],
            description = row[TagTable.description]
        )
    }
}
