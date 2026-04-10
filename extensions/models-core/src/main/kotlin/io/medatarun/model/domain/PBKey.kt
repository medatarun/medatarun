package io.medatarun.model.domain

interface PBKey {

    /**
     * Identifier of the primary of business key
     */
    val id: EntityPBKeyId

    /**
     * Entity for which the key exists
     */
    val entityId: EntityId

    /**
     * Indicate this key is the primary key or a business key
     */
    val kind: PBKeyKind

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