package io.medatarun.model.domain

/**
 * Attribute definition for an [EntityDef]
 */
interface AttributeDef {
    /**
     * Unique key of the attribute in its [EntityDef]
     */
    val key: AttributeKey

    /**
     * Display name of the attribute
     */
    val name: LocalizedText?

    /**
     * Display description of the attribute
     */
    val description: LocalizedMarkdown?

    /**
     * Type of attribute, must be one of the types registered in the model
     */
    val type: TypeKey

    /**
     * Indicates that this attribute is optional in Entities (default is that attributes are required).
     */
    val optional: Boolean

    /**
     * Tags added to this attribute for categorization
     */
    val hashtags: List<Hashtag>
}