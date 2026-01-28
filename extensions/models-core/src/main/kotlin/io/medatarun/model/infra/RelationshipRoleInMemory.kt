package io.medatarun.model.infra

import io.medatarun.model.domain.*

data class RelationshipRoleInMemory(
    override val id: RelationshipRoleId,
    override val key: RelationshipRoleKey,
    override val entityId: EntityId,
    override val name: LocalizedText?,
    override val cardinality: RelationshipCardinality
) : RelationshipRole {
    companion object {
        fun of(other: RelationshipRole): RelationshipRoleInMemory {
            return RelationshipRoleInMemory(
                id = other.id,
                key = other.key,
                entityId = other.entityId,
                name = other.name,
                cardinality = other.cardinality

            )
        }
    }
}
