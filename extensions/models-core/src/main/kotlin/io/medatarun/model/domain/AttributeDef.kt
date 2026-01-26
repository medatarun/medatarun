package io.medatarun.model.domain

/**
 * Attribute definition for an [EntityDef]
 */
interface AttributeDef {
    /**
     * Unique identifier in the application instance and more generally across all instances since it is backed by UUID
     */
    val id: AttributeId

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
    val typeId: TypeId

    /**
     * Indicates that this attribute is optional in Entities (default is that attributes are required).
     */
    val optional: Boolean

    /**
     * Tags added to this attribute for categorization
     */
    val hashtags: List<Hashtag>
}