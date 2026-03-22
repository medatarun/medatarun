package io.medatarun.model.domain

/**
 * Location of a tag usage inside the current head snapshot of a model.
 *
 * This type only models places where a tag can actually be attached and therefore needs cleanup
 * when that tag is deleted.
 */
sealed class DomainTagLocation {
    data class Model(val modelId: ModelId) : DomainTagLocation()

    data class Entity(
        val modelId: ModelId,
        val entityId: EntityId
    ) : DomainTagLocation()

    data class EntityAttribute(
        val modelId: ModelId,
        val entityId: EntityId,
        val attributeId: AttributeId
    ) : DomainTagLocation()

    data class Relationship(
        val modelId: ModelId,
        val relationshipId: RelationshipId
    ) : DomainTagLocation()

    data class RelationshipAttribute(
        val modelId: ModelId,
        val relationshipId: RelationshipId,
        val attributeId: AttributeId
    ) : DomainTagLocation()
}
