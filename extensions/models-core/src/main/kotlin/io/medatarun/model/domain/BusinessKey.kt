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
    val name: String?

    /**
     * Description of the key
     */
    val description: String?
}