package io.medatarun.model.domain.search

import io.medatarun.tags.core.domain.TagRef

data class SearchFilters(
    val operator: SearchFiltersLogicalOperator,
    val filters: List<SearchFilter>
)

enum class SearchFiltersLogicalOperator(val code: String) {
    AND("and"), OR("or");

    companion object {
        fun valueOfCodeOptional(code: String): SearchFiltersLogicalOperator? {
            return entries.firstOrNull { it.code == code }
        }
    }
}

sealed class SearchFilter(
    val type: SearchFilterType
)

sealed class SearchFilterTags : SearchFilter(SearchFilterType("tags")) {
    object Empty : SearchFilterTags()
    object NotEmpty : SearchFilterTags()
    data class AnyOf(val names: List<TagRef>) : SearchFilterTags()
    data class NoneOf(val names: List<TagRef>) : SearchFilterTags()
    data class AllOf(val names: List<TagRef>) : SearchFilterTags()
}