package io.medatarun.tags.core.domain

import io.medatarun.type.commons.enums.EnumWithCode

data class TagSearchFilters(
    val operator: TagSearchFiltersLogicalOperator,
    val items: List<TagSearchFilter>
)

enum class TagSearchFiltersLogicalOperator(override val code: String): EnumWithCode {
    AND("and"),
    OR("or");

    companion object {
        fun valueOfCodeOptional(code: String): TagSearchFiltersLogicalOperator? {
            return entries.firstOrNull { it.code == code }
        }
    }
}

sealed class TagSearchFilter(
    val type: TagSearchFilterType
)

@JvmInline
value class TagSearchFilterType(val value: String)

sealed class TagSearchFilterScopeRef : TagSearchFilter(TagSearchFilterType("scopeRef")) {
    data class Is(val value: TagScopeRef) : TagSearchFilterScopeRef()
}
