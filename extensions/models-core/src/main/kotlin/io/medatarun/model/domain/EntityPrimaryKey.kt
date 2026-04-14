package io.medatarun.model.domain

interface EntityPrimaryKey {

    /**
     * Identifier of the primary key
     */
    val id: EntityPrimaryKeyId

    /**
     * Entity for which the key exists
     */
    val entityId: EntityId

    /**
     * Ordered set of attributes that contribute to the key
     */
    val participants: List<PBKeyParticipant>

    /**
     * Returns true when this primary key contains exactly the provided attributes,
     * in the same order.
     */
    fun containsInOrder(attributeIds: List<AttributeId>): Boolean {
        val orderedAttributeIds = participants
            .sortedBy { participant -> participant.position }
            .map { participant -> participant.attributeId }
        return orderedAttributeIds == attributeIds
    }

}
