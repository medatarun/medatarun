package io.medatarun.model.domain

@JvmInline
value class AttributeKey(val value: String) {
    fun validated(): AttributeKey {
        return this
    }
}

/**
 * Attribute definition for an [EntityDef]
 */
interface AttributeDef {
    /**
     * Unique identifier of the attribute in its [EntityDef]
     */
    val id: AttributeKey

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