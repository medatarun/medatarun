package io.medatarun.model.domain.search

enum class SearchField(val code: String) {
    LOCATION("location");

    companion object {
        fun valueOfCodeOptional(code: String): SearchField? {
            return entries.firstOrNull { it.code == code }
        }
    }
}