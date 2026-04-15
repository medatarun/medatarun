package io.medatarun.model.domain

interface BusinessKey {

    /**
     * Identifier of the primary of business key
     */
    val id: BusinessKeyId

    /**
     * Unique key of the business key in the model
     */
    val key: BusinessKeyKey

    /**
     * Entity for which the key exists
     */
    val entityId: EntityId

    /**
     * Ordered set of attributes that contribute to the key
     */
    val participants: List<PBKeyParticipant>

    /**
     * Name of the key
     */
    val name: LocalizedText?

    /**
     * Description of the key
     */
    val description: LocalizedMarkdown?

    /**
     * Returns true when this business key contains exactly the provided attributes,
     * in the same order.
     */
    fun containsInOrder(attributeIds: List<AttributeId>): Boolean {
        val orderedAttributeIds = participants
            .sortedBy { participant -> participant.position }
            .map { participant -> participant.attributeId }
        return orderedAttributeIds == attributeIds
    }
}
