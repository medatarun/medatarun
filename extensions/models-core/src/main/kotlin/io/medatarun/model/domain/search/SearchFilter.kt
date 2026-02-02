package io.medatarun.model.domain.search

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
    data class AnyOf(val names: List<String>) : SearchFilterTags()
    data class NoneOf(val names: List<String>) : SearchFilterTags()
    data class AllOf(val names: List<String>) : SearchFilterTags()
}