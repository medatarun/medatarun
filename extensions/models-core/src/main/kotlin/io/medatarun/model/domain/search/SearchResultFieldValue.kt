package io.medatarun.model.domain.search

import io.medatarun.model.domain.DomainLocation

sealed interface SearchResultFieldValue
data class SearchResultFieldValueLocation(
    val location: DomainLocation
) : SearchResultFieldValue
