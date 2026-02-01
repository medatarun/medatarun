package io.medatarun.model.domain

sealed class DomainLocation(val objectType: String)

data class ModelLocation(
    val id: ModelId,
    val key: ModelKey,
    val label: String
) : DomainLocation("model")

data class TypeLocation(
    val model: ModelLocation,
    val id: EntityId,
    val key: EntityKey,
    val label: String
) : DomainLocation("type")

data class EntityLocation(
    val model: ModelLocation,
    val id: EntityId,
    val key: EntityKey,
    val label: String
) : DomainLocation("entity")

data class EntityAttributeLocation(
    val entity: EntityLocation,
    val id: AttributeId,
    val key: AttributeKey,
    val label: String
) : DomainLocation("entityAttribute")

data class RelationshipLocation(
    val model: ModelLocation,
    val id: RelationshipId,
    val key: RelationshipKey,
    val label: String
) : DomainLocation("relationship")

data class RelationshipAttributeLocation(
    val relationship: RelationshipLocation,
    val id: AttributeId,
    val key: AttributeKey,
    val label: String
) : DomainLocation("relationshipAttribute")