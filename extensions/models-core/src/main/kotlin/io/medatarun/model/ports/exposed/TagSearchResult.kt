package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.*

data class TagSearchResult(
    /**
     * Unique identifier for this search result
     */
    val id: String,
    val locationType: String,
    /**
     * Location when in a model
     */
    val modelId: ModelId,
    val modelLabel: String,
    /**
     * Location when in an entity
     */
    val entityId: EntityId? = null,
    val entityLabel: String? = null,
    /**
     * Location when in an entity attribute
     */
    val entityAttributeId: AttributeId? = null,
    val entityAttributeLabel: String? = null,
    /**
     * Location when in an entity
     */
    val relationshipId: RelationshipId? = null,
    val relationshipLabel: String? = null,
    /**
     * Location when in an entity attribute
     */
    val relationshipAttributeId: AttributeId? = null,
    val relationshipAttributeLabel: String? = null,

    val tags: List<Hashtag>


    )