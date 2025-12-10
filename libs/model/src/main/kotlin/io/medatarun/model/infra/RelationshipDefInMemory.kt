package io.medatarun.model.infra

import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.RelationshipDef
import io.medatarun.model.domain.RelationshipDefId

data class RelationshipDefInMemory(
    override val id: RelationshipDefId,
    override val name: LocalizedText?,
    override val description: LocalizedText?,
    override val roles: List<RelationshipRoleInMemory>,
    override val attributes: List<AttributeDefInMemory>
) : RelationshipDef {
    companion object {
        fun of(other: RelationshipDef): RelationshipDefInMemory {
            return RelationshipDefInMemory(
            id = other.id,
                name = other.name,
                description = other.description,
                roles = other.roles.map(RelationshipRoleInMemory::of),
                attributes = other.attributes.map(AttributeDefInMemory::of)
            )
        }
    }
}
