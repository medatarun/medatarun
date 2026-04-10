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

}