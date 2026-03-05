package io.medatarun.model.domain

sealed interface AttributeOwnerId {
    data class OwnerEntityId(val id: EntityId): AttributeOwnerId
    data class OwnerRelationshipId(val id: RelationshipId):AttributeOwnerId
}
