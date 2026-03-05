package io.medatarun.model.ports.needs

import io.medatarun.model.domain.search.SearchFields
import io.medatarun.model.domain.search.SearchFilterType
import io.medatarun.model.domain.search.SearchFiltersLogicalOperator
import io.medatarun.tags.core.domain.TagId

/**
 * Equivalent of [io.medatarun.model.domain.search.SearchQuery] tailored to storage queries.
 *
 * The main difference is that references are already resolved to ids (for example, tags references).
 *
 * This way so that the query stays simple and doesn't need to cross this module data boundary
 * (typically Models don't need to look into tags data).
 */
data class ModelStorageSearchQuery(
    val filters: ModelStorageSearchFilters,
    val fields: SearchFields
)

data class ModelStorageSearchFilters(
    val operator: SearchFiltersLogicalOperator,
    val items: List<ModelStorageSearchFilter>
)

sealed class ModelStorageSearchFilter(
    val type: SearchFilterType
)

sealed class ModelStorageSearchFilterTags : ModelStorageSearchFilter(SearchFilterType("tags")) {
    object Empty : ModelStorageSearchFilterTags()
    object NotEmpty : ModelStorageSearchFilterTags()
    data class AnyOf(val names: List<TagId>) : ModelStorageSearchFilterTags()
    data class NoneOf(val names: List<TagId>) : ModelStorageSearchFilterTags()
    data class AllOf(val names: List<TagId>) : ModelStorageSearchFilterTags()
}

sealed class ModelStorageSearchFilterText : ModelStorageSearchFilter(SearchFilterType("text")) {
    data class Contains(val value: String) : ModelStorageSearchFilterText()
}
