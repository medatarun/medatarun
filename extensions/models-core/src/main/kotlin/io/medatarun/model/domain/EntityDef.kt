package io.medatarun.model.domain

import java.net.URL

/**
 * Definition of an Entity.
 */
interface EntityDef {
    /**
     * Unique identifier in the application instance and more generally across all instances since it is backed by UUID
     */
    val id: EntityId
    /**
     * Unique key of this EntityDef in the [Model]
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
     * Attributes contained in this EntityDef
     */
    val attributes: List<AttributeDef>

    @Deprecated("Use identifierAttributeId instead.", ReplaceWith("identifierAttributeId"))
    val identifierAttributeKey: AttributeKey
    /**
     * Tells which attribute acts as entities' identifier
     */
    val identifierAttributeId: AttributeId

    /**
     * Tells where the definition comes from
     */
    val origin: EntityOrigin

    /**
     * Documentation home
     */
    val documentationHome: URL?
    /**
     * Hashtags used for classification
     */
    val hashtags: List<Hashtag>


    /**
     * Get attribute by its id if found
     */
    fun getAttributeDefOptional(id: AttributeKey): AttributeDef?


    /**
     * Returns the attribute name that serves as entity unique identifier
     * amongst other entities in the same [EntityDef].
     */
    fun entityIdAttributeDefId() = AttributeKey("id")


    // TESTS ----------------



}