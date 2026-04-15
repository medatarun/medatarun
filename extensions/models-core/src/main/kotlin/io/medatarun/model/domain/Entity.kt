package io.medatarun.model.domain

import io.medatarun.tags.core.domain.TagId
import java.net.URL

/**
 * Definition of an Entity.
 */
interface Entity {
    /**
     * Unique identifier in the application instance and more generally across all instances since it is backed by UUID
     */
    val id: EntityId

    /**
     * Unique key of this [Entity] in the [ModelAggregate]
     */
    val key: EntityKey

    /**
     * Display name
     */
    val name: LocalizedText?

    /**
     * Display description
     */
    val description: LocalizedMarkdown?

    /**
     * Tells where the definition comes from
     */
    val origin: EntityOrigin

    /**
     * Documentation home
     */
    val documentationHome: URL?

    /**
     * Tags used for classification
     */
    val tags: List<TagId>

    val ref get() = EntityRef.ById(id)


}
