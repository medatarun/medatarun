package io.medatarun.model.domain.search

import io.medatarun.model.domain.DomainLocation

data class SearchResultItem(
    /**
     * Each search result item must have a unique identifier
     */
    val id: String,
    /**
     * Each search result item points to a domain object, so we specify the object location
     */
    val location: DomainLocation,
    val fields: Map<String, SearchResultFieldValue>
) {

}

