package io.medatarun.model.infra

import io.medatarun.model.domain.*

data class RelationshipRoleInMemory(
    override val id: RelationshipRoleKey,
    override val entityId: EntityKey,
    override val name: LocalizedText?,
    override val cardinality: RelationshipCardinality
) : RelationshipRole {
    companion object {
        fun of(other: RelationshipRole): RelationshipRoleInMemory {
            return RelationshipRoleInMemory(
                id = other.id,
                entityId = other.entityId,
                name = other.name,
                cardinality = other.cardinality

            )
        }
    }
}
