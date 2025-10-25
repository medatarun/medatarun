package io.medatarun.model.model

@JvmInline
value class EntityDefId(val value: String)

/**
 * Definition of an Entity.
 */
interface EntityDef {
    /**
     * Unique identifier of this EntityDef in the [Model]
     */
    val id: EntityDefId

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

    /**
     * Tells which attribute acts as entities' identifier
     */
    val identifierAttributeDefId: AttributeDefId

    /**
     * Count attributes
     */
    fun countAttributeDefs(): Int

    /**
     * Get attribute by its id. Throws [AttributeDefNotFoundException] otherwise.
     */
    fun getAttributeDef(id: AttributeDefId): AttributeDef

    /**
     * @return true if this entity contains this attribute
     */
    fun hasAttributeDef(id: AttributeDefId): Boolean

    /**
     * Ensures that an attribute exists or throws [AttributeDefNotFoundException] otherwise.
     * This is syntax sugar around [getAttributeDef]
     */
    fun ensureAttributeDefExists(id: AttributeDefId) {
        // Ensures attribute definition exist, syntax sugar
        getAttributeDef(id)
    }

    /**
     * Returns the attribute name that serves as entity unique identifier
     * amongst other entities in the same [EntityDef].
     */
    fun entityIdAttributeDefId() = AttributeDefId("id")



}