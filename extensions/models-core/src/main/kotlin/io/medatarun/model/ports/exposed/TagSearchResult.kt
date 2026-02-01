package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.*

data class TagSearchResult(
    /**
     * Unique identifier for this search result
     */
    val id: String,
    val location: DomainLocation,
    val tags: List<Hashtag>
)