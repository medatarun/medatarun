package io.medatarun.model.infra.db

import io.medatarun.model.domain.search.SearchFiltersLogicalOperator
import io.medatarun.model.domain.search.SearchResultItem
import io.medatarun.model.domain.search.SearchResults
import io.medatarun.model.domain.search.normalizeModelSearchText
import io.medatarun.model.infra.db.records.DenormModelSearchItemRecord
import io.medatarun.model.infra.db.records.DenormModelSearchItemTagRecord
import io.medatarun.model.infra.db.tables.DenormModelSearchItemTable
import io.medatarun.model.infra.db.tables.DenormModelSearchItemTagTable
import io.medatarun.model.ports.needs.ModelStorageSearchFilter
import io.medatarun.model.ports.needs.ModelStorageSearchFilterTags
import io.medatarun.model.ports.needs.ModelStorageSearchFilterText
import io.medatarun.model.ports.needs.ModelStorageSearchQuery
import io.medatarun.platform.db.DbConnectionFactory
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.notInSubQuery
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

/**
 * Cleary not good at all, need to redo this properly to get everything in one query
 */
internal class ModelStorageDbSearchRead(
    private val dbConnectionFactory: DbConnectionFactory
) {
    fun search(query: ModelStorageSearchQuery): SearchResults {
        return dbConnectionFactory.withExposed { searchT(query) }
    }

    private fun searchT(query: ModelStorageSearchQuery): SearchResults {
        val matchingIds = resolveMatchingSearchItemIds(query)
        if (matchingIds.isEmpty()) {
            return SearchResults(emptyList())
        }

        val rows = DenormModelSearchItemTable.selectAll()
            .where { DenormModelSearchItemTable.id inList matchingIds.toList() }
            .orderBy(
                DenormModelSearchItemTable.attributeLabel to SortOrder.ASC,
                DenormModelSearchItemTable.relationshipLabel to SortOrder.ASC,
                DenormModelSearchItemTable.entityLabel to SortOrder.ASC,
                DenormModelSearchItemTable.modelLabel to SortOrder.ASC
            )
            .toList()
            .map { DenormModelSearchItemRecord.read(it) }

        return SearchResults(
            rows.map { item ->
                SearchResultItem(
                    id = item.id,
                    location = item.toDomainLocation(),
                    fields = emptyMap()
                )
            }
        )
    }

    private fun resolveMatchingSearchItemIds(query: ModelStorageSearchQuery): Set<String> {
        if (query.filters.items.isEmpty()) {
            return when (query.filters.operator) {
                SearchFiltersLogicalOperator.AND -> {
                    DenormModelSearchItemTable
                        .select(DenormModelSearchItemTable.id)
                        .map { it[DenormModelSearchItemTable.id] }
                        .toSet()
                }

                SearchFiltersLogicalOperator.OR -> emptySet()
            }
        }

        val filterMatches = query.filters.items.map { resolveFilterMatchIds(it) }
        return when (query.filters.operator) {
            SearchFiltersLogicalOperator.AND -> {
                val first = filterMatches.first()
                filterMatches.drop(1).fold(first) { acc, current -> acc.intersect(current) }
            }

            SearchFiltersLogicalOperator.OR -> {
                filterMatches.fold(emptySet()) { acc, current -> acc.union(current) }
            }
        }
    }

    private fun resolveFilterMatchIds(filter: ModelStorageSearchFilter): Set<String> {
        return when (filter) {
            is ModelStorageSearchFilterTags.Empty -> selectSearchItemIdsWithoutTags()
            is ModelStorageSearchFilterTags.NotEmpty -> selectSearchItemIdsWithTags()
            is ModelStorageSearchFilterTags.AnyOf -> selectSearchItemIdsWithAnyTag(filter)
            is ModelStorageSearchFilterTags.NoneOf -> selectSearchItemIdsWithNoneOfTags(filter)
            is ModelStorageSearchFilterTags.AllOf -> selectSearchItemIdsWithAllTags(filter)
            is ModelStorageSearchFilterText.Contains -> selectSearchItemIdsByContains(filter)
        }
    }

    private fun selectSearchItemIdsWithoutTags(): Set<String> {
        return DenormModelSearchItemTable
            .select(DenormModelSearchItemTable.id)
            .where {
                DenormModelSearchItemTable.id notInSubQuery
                        DenormModelSearchItemTagTable
                            .select(DenormModelSearchItemTagTable.searchItemId)
            }
            .map { it[DenormModelSearchItemTable.id] }
            .toSet()
    }

    private fun selectSearchItemIdsWithTags(): Set<String> {
        return DenormModelSearchItemTagTable
            .select(DenormModelSearchItemTagTable.searchItemId)
            .map { it[DenormModelSearchItemTagTable.searchItemId] }
            .toSet()
    }

    private fun selectSearchItemIdsWithAnyTag(
        filter: ModelStorageSearchFilterTags.AnyOf
    ): Set<String> {
        val tagIds = filter.names
        if (tagIds.isEmpty()) {
            return emptySet()
        }

        return DenormModelSearchItemTagTable
            .select(DenormModelSearchItemTagTable.searchItemId)
            .where { DenormModelSearchItemTagTable.tagId inList tagIds.map { it.asString() } }
            .map { it[DenormModelSearchItemTagTable.searchItemId] }
            .toSet()
    }

    private fun selectSearchItemIdsWithNoneOfTags(
        filter: ModelStorageSearchFilterTags.NoneOf
    ): Set<String> {
        val tagIds = filter.names
        if (tagIds.isEmpty()) {
            return DenormModelSearchItemTable
                .select(DenormModelSearchItemTable.id)
                .map { it[DenormModelSearchItemTable.id] }
                .toSet()
        }

        return DenormModelSearchItemTable
            .select(DenormModelSearchItemTable.id)
            .where {
                DenormModelSearchItemTable.id notInSubQuery
                        DenormModelSearchItemTagTable
                            .select(DenormModelSearchItemTagTable.searchItemId)
                            .where { DenormModelSearchItemTagTable.tagId inList tagIds.map { it.asString() } }
            }
            .map { it[DenormModelSearchItemTable.id] }
            .toSet()
    }

    private fun selectSearchItemIdsWithAllTags(
        filter: ModelStorageSearchFilterTags.AllOf
    ): Set<String> {
        val tagIds = filter.names
        if (tagIds.isEmpty()) {
            return DenormModelSearchItemTable
                .select(DenormModelSearchItemTable.id)
                .map { it[DenormModelSearchItemTable.id] }
                .toSet()
        }

        val rows = DenormModelSearchItemTagTable.selectAll()
            .where { DenormModelSearchItemTagTable.tagId inList tagIds.map { it.asString() } }
            .toList()
            .map { DenormModelSearchItemTagRecord.read(it) }

        return rows
            .groupBy { it.searchItemId }
            .filterValues { itemRows ->
                itemRows.map { it.tagId }
                    .distinct()
                    .size == tagIds.size
            }
            .keys
    }

    private fun selectSearchItemIdsByContains(filter: ModelStorageSearchFilterText.Contains): Set<String> {
        val searchedText = normalizeModelSearchText(filter.value)
        if (searchedText.isBlank()) {
            return DenormModelSearchItemTable
                .select(DenormModelSearchItemTable.id)
                .map { it[DenormModelSearchItemTable.id] }
                .toSet()
        }

        val pattern = "%" + escapeLikePattern(searchedText) + "%"
        return DenormModelSearchItemTable
            .select(DenormModelSearchItemTable.id)
            .where { DenormModelSearchItemTable.searchText like pattern }
            .map { it[DenormModelSearchItemTable.id] }
            .toSet()
    }



    private fun escapeLikePattern(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
    }
}
