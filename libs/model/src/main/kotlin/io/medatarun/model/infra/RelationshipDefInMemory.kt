package io.medatarun.model.infra

import io.medatarun.model.model.AttributeDef
import io.medatarun.model.model.LocalizedText
import io.medatarun.model.model.RelationshipDef
import io.medatarun.model.model.RelationshipDefId
import io.medatarun.model.model.RelationshipRole

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
