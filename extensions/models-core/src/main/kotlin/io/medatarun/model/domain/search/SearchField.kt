package io.medatarun.model.domain.search

import io.medatarun.type.commons.enums.EnumWithCode

enum class SearchField(override val code: String): EnumWithCode {
    LOCATION("location");

    companion object {
        fun valueOfCodeOptional(code: String): SearchField? {
            return entries.firstOrNull { it.code == code }
        }
    }
}