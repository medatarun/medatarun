package io.medatarun.tags.core.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.infra.db.tables.TagViewHistory_TagGroup_Table
import io.medatarun.tags.core.infra.db.tables.TagViewCurrent_TagGroup_Table
import io.medatarun.tags.core.infra.db.tables.TagViewHistory_Tag_Table
import io.medatarun.tags.core.infra.db.tables.TagViewCurrent_Tag_Table
import io.medatarun.tags.core.internal.TagGroupInMemory
import io.medatarun.tags.core.internal.TagInMemory
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.time.Instant

internal class TagStorageDbRead {
    private class TagStorageDbInvalidGlobalLookupException : MedatarunException("Global tag lookup requires groupId")

    fun findAllTag(): List<Tag> {
        return TagViewCurrent_Tag_Table.selectAll().map { row -> tagFromRow(row) }
    }

    fun findAllTagByScopeRef(scopeRef: TagScopeRef): List<Tag> {
        return when (scopeRef) {
            is TagScopeRef.Global -> {
                TagViewCurrent_Tag_Table.selectAll().where {
                    (TagViewCurrent_Tag_Table.scopeType eq TagScopeRef.Global.type.value) and TagViewCurrent_Tag_Table.scopeId.isNull()
                }.map { row -> tagFromRow(row) }
            }

            is TagScopeRef.Local -> {
                TagViewCurrent_Tag_Table.selectAll().where {
                    (TagViewCurrent_Tag_Table.scopeType eq scopeRef.type.value) and
                            (TagViewCurrent_Tag_Table.scopeId eq scopeRef.localScopeId)
                }.map { row -> tagFromRow(row) }
            }
        }
    }

    fun search(query: TagSearchFilters): List<Tag> {

        // Transforms the search filters to Exposed criterion list
        val criterionList = query.items.map { item ->
            when (item) {
                is TagSearchFilterScopeRef.Is -> createCriterionScope(item.value)
            }
        }

        // Join the individual criteria with "AND" or "OR" depending on
        // the search query
        var predicateChain: Op<Boolean>? = null
        for (currentCriterion in criterionList) {
            predicateChain = if (predicateChain == null) {
                currentCriterion
            } else {
                when (query.operator) {
                    TagSearchFiltersLogicalOperator.AND -> predicateChain and currentCriterion
                    TagSearchFiltersLogicalOperator.OR -> predicateChain or currentCriterion
                }
            }
        }

        // If we don't have any criterion, then return empty list
        // because we don't know what the caller wants
        val predicate = predicateChain ?: return emptyList()

        // Build the query, get results and map them to tags
        return TagViewCurrent_Tag_Table
            .selectAll().where { predicate }
            .map { row -> tagFromRow(row) }
    }

    private fun createCriterionScope(scopeRef: TagScopeRef): Op<Boolean> {
        return when (scopeRef) {
            is TagScopeRef.Global -> {
                (TagViewCurrent_Tag_Table.scopeType eq TagScopeRef.Global.type.value) and TagViewCurrent_Tag_Table.scopeId.isNull()
            }

            is TagScopeRef.Local -> {
                (TagViewCurrent_Tag_Table.scopeType eq scopeRef.type.value) and
                        (TagViewCurrent_Tag_Table.scopeId eq scopeRef.localScopeId)
            }
        }
    }

    fun findTagByKeyOptional(scope: TagScopeRef, groupId: TagGroupId?, key: TagKey): Tag? {
        return when (scope) {
            is TagScopeRef.Local -> {
                TagViewCurrent_Tag_Table
                    .selectAll()
                    .where {
                        (TagViewCurrent_Tag_Table.scopeType eq scope.type.value) and
                                (TagViewCurrent_Tag_Table.scopeId eq scope.localScopeId) and
                                TagViewCurrent_Tag_Table.tagGroupId.isNull() and
                                (TagViewCurrent_Tag_Table.key eq key)
                    }
                    .singleOrNull()
                    ?.let { row -> tagFromRow(row) }
            }

            is TagScopeRef.Global -> {
                val effectiveGroupId = groupId ?: throw TagStorageDbInvalidGlobalLookupException()
                TagViewCurrent_Tag_Table
                    .selectAll()
                    .where {
                        (TagViewCurrent_Tag_Table.scopeType eq scope.type.value) and
                                TagViewCurrent_Tag_Table.scopeId.isNull() and
                                (TagViewCurrent_Tag_Table.tagGroupId eq effectiveGroupId) and
                                (TagViewCurrent_Tag_Table.key eq key)
                    }
                    .singleOrNull()
                    ?.let { row -> tagFromRow(row) }
            }
        }
    }

    fun findTagByIdOptional(id: TagId): Tag? {
        return TagViewCurrent_Tag_Table
            .selectAll()
            .where { TagViewCurrent_Tag_Table.id eq id }
            .singleOrNull()
            ?.let { row -> tagFromRow(row) }
    }

    fun findTagByIdAsOfOptional(id: TagId, eventDate: Instant): Tag? {
        return TagViewHistory_Tag_Table
            .selectAll()
            .where {
                (TagViewHistory_Tag_Table.tagId eq id) and
                        (TagViewHistory_Tag_Table.validFrom lessEq eventDate) and
                        (TagViewHistory_Tag_Table.validTo.isNull() or (TagViewHistory_Tag_Table.validTo greater eventDate))
            }
            .singleOrNull()
            ?.let { row ->
                val scopeType = TagScopeType(row[TagViewHistory_Tag_Table.scopeType])
                val scopeId = row[TagViewHistory_Tag_Table.scopeId]
                val scope = if (scopeType.value == TagScopeRef.Global.type.value) {
                    TagScopeRef.Global
                } else {
                    val localScopeId = requireNotNull(scopeId) {
                        "Local tag history row missing scope_id"
                    }
                    TagScopeRef.Local(scopeType, localScopeId)
                }
                TagInMemory(
                    id = row[TagViewHistory_Tag_Table.tagId],
                    scope = scope,
                    groupId = row[TagViewHistory_Tag_Table.tagGroupId],
                    key = row[TagViewHistory_Tag_Table.key],
                    name = row[TagViewHistory_Tag_Table.name],
                    description = row[TagViewHistory_Tag_Table.description]
                )
            }
    }

    fun findAllTagGroup(): List<TagGroup> {
        return TagViewCurrent_TagGroup_Table
            .selectAll()
            .map { row -> tagGroupFromRow(row) }
    }

    fun findTagGroupByIdOptional(id: TagGroupId): TagGroup? {
        return TagViewCurrent_TagGroup_Table
            .selectAll()
            .where { TagViewCurrent_TagGroup_Table.id eq id }
            .singleOrNull()
            ?.let { row -> tagGroupFromRow(row) }
    }

    fun findTagGroupByIdAsOfOptional(id: TagGroupId, eventDate: Instant): TagGroup? {
        return TagViewHistory_TagGroup_Table
            .selectAll()
            .where {
                (TagViewHistory_TagGroup_Table.tagGroupId eq id) and
                        (TagViewHistory_TagGroup_Table.validFrom lessEq eventDate) and
                        (TagViewHistory_TagGroup_Table.validTo.isNull() or (TagViewHistory_TagGroup_Table.validTo greater eventDate))
            }
            .singleOrNull()
            ?.let { row ->
                TagGroupInMemory(
                    id = row[TagViewHistory_TagGroup_Table.tagGroupId],
                    key = row[TagViewHistory_TagGroup_Table.key],
                    name = row[TagViewHistory_TagGroup_Table.name],
                    description = row[TagViewHistory_TagGroup_Table.description]
                )
            }
    }

    fun findTagGroupByKeyOptional(key: TagGroupKey): TagGroup? {
        return TagViewCurrent_TagGroup_Table
            .selectAll()
            .where { TagViewCurrent_TagGroup_Table.key eq key }
            .singleOrNull()
            ?.let { row -> tagGroupFromRow(row) }
    }

    private fun tagGroupFromRow(row: ResultRow): TagGroup {
        return TagGroupInMemory(
            id = row[TagViewCurrent_TagGroup_Table.id],
            key = row[TagViewCurrent_TagGroup_Table.key],
            name = row[TagViewCurrent_TagGroup_Table.name],
            description = row[TagViewCurrent_TagGroup_Table.description]
        )
    }

    private fun tagFromRow(row: ResultRow): Tag {
        val scopeType = TagScopeType(row[TagViewCurrent_Tag_Table.scopeType])
        val scopeId = row[TagViewCurrent_Tag_Table.scopeId]
        val scope = if (scopeType.value == TagScopeRef.Global.type.value) {
            TagScopeRef.Global
        } else {
            val localScopeId = requireNotNull(scopeId) {
                "Local tag row missing scope_id"
            }
            TagScopeRef.Local(scopeType, localScopeId)
        }
        val groupId = row[TagViewCurrent_Tag_Table.tagGroupId]
        return TagInMemory(
            id = row[TagViewCurrent_Tag_Table.id],
            scope = scope,
            groupId = groupId,
            key = row[TagViewCurrent_Tag_Table.key],
            name = row[TagViewCurrent_Tag_Table.name],
            description = row[TagViewCurrent_Tag_Table.description]
        )
    }

}
