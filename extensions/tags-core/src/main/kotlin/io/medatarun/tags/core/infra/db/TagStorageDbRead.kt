package io.medatarun.tags.core.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.tags.core.domain.*
import io.medatarun.tags.core.infra.db.tables.TagGroupProjectionTable
import io.medatarun.tags.core.infra.db.tables.TagProjectionTable
import io.medatarun.tags.core.internal.TagGroupInMemory
import io.medatarun.tags.core.internal.TagInMemory
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.selectAll

internal class TagStorageDbRead {
    private class TagStorageDbInvalidGlobalLookupException : MedatarunException("Global tag lookup requires groupId")

    fun findAllTag(): List<Tag> {
        return TagProjectionTable.selectAll().map { row -> tagFromRow(row) }
    }

    fun findAllTagByScopeRef(scopeRef: TagScopeRef): List<Tag> {
        return when (scopeRef) {
            is TagScopeRef.Global -> {
                TagProjectionTable.selectAll().where {
                    (TagProjectionTable.scopeType eq TagScopeRef.Global.type.value) and TagProjectionTable.scopeId.isNull()
                }.map { row -> tagFromRow(row) }
            }

            is TagScopeRef.Local -> {
                TagProjectionTable.selectAll().where {
                    (TagProjectionTable.scopeType eq scopeRef.type.value) and
                            (TagProjectionTable.scopeId eq scopeRef.localScopeId)
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
        return TagProjectionTable
            .selectAll().where { predicate }
            .map { row -> tagFromRow(row) }
    }

    private fun createCriterionScope(scopeRef: TagScopeRef): Op<Boolean> {
        return when (scopeRef) {
            is TagScopeRef.Global -> {
                (TagProjectionTable.scopeType eq TagScopeRef.Global.type.value) and TagProjectionTable.scopeId.isNull()
            }

            is TagScopeRef.Local -> {
                (TagProjectionTable.scopeType eq scopeRef.type.value) and
                        (TagProjectionTable.scopeId eq scopeRef.localScopeId)
            }
        }
    }

    fun findTagByKeyOptional(scope: TagScopeRef, groupId: TagGroupId?, key: TagKey): Tag? {
        return when (scope) {
            is TagScopeRef.Local -> {
                TagProjectionTable
                    .selectAll()
                    .where {
                        (TagProjectionTable.scopeType eq scope.type.value) and
                                (TagProjectionTable.scopeId eq scope.localScopeId) and
                                TagProjectionTable.tagGroupId.isNull() and
                                (TagProjectionTable.key eq key)
                    }
                    .singleOrNull()
                    ?.let { row -> tagFromRow(row) }
            }

            is TagScopeRef.Global -> {
                val effectiveGroupId = groupId ?: throw TagStorageDbInvalidGlobalLookupException()
                TagProjectionTable
                    .selectAll()
                    .where {
                        (TagProjectionTable.scopeType eq scope.type.value) and
                                TagProjectionTable.scopeId.isNull() and
                                (TagProjectionTable.tagGroupId eq effectiveGroupId) and
                                (TagProjectionTable.key eq key)
                    }
                    .singleOrNull()
                    ?.let { row -> tagFromRow(row) }
            }
        }
    }

    fun findTagByIdOptional(id: TagId): Tag? {
        return TagProjectionTable
            .selectAll()
            .where { TagProjectionTable.id eq id }
            .singleOrNull()
            ?.let { row -> tagFromRow(row) }
    }

    fun findAllTagGroup(): List<TagGroup> {
        return TagGroupProjectionTable
            .selectAll()
            .map { row -> tagGroupFromRow(row) }
    }

    fun findTagGroupByIdOptional(id: TagGroupId): TagGroup? {
        return TagGroupProjectionTable
            .selectAll()
            .where { TagGroupProjectionTable.id eq id }
            .singleOrNull()
            ?.let { row -> tagGroupFromRow(row) }
    }

    fun findTagGroupByKeyOptional(key: TagGroupKey): TagGroup? {
        return TagGroupProjectionTable
            .selectAll()
            .where { TagGroupProjectionTable.key eq key }
            .singleOrNull()
            ?.let { row -> tagGroupFromRow(row) }
    }

    private fun tagGroupFromRow(row: ResultRow): TagGroup {
        return TagGroupInMemory(
            id = row[TagGroupProjectionTable.id],
            key = row[TagGroupProjectionTable.key],
            name = row[TagGroupProjectionTable.name],
            description = row[TagGroupProjectionTable.description]
        )
    }

    private fun tagFromRow(row: ResultRow): Tag {
        val scopeType = TagScopeType(row[TagProjectionTable.scopeType])
        val scopeId = row[TagProjectionTable.scopeId]
        val scope = if (scopeType.value == TagScopeRef.Global.type.value) {
            TagScopeRef.Global
        } else {
            val localScopeId = requireNotNull(scopeId) {
                "Local tag row missing scope_id"
            }
            TagScopeRef.Local(scopeType, localScopeId)
        }
        val groupId = row[TagProjectionTable.tagGroupId]
        return TagInMemory(
            id = row[TagProjectionTable.id],
            scope = scope,
            groupId = groupId,
            key = row[TagProjectionTable.key],
            name = row[TagProjectionTable.name],
            description = row[TagProjectionTable.description]
        )
    }
}
