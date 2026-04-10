package io.medatarun.model.domain

/**
 * Participant in a primary or business key
 */
interface PBKeyParticipant {
    /**
     * Attribute that participates in the primary or business key
     */
    val attributeId: AttributeId

    /**
     * Position of the attribute in the primary or business key. This allows ordering the key elements.
     */
    val position: Int
}