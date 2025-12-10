package io.medatarun.model.infra

import io.medatarun.model.domain.Hashtag
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.RelationshipDef
import io.medatarun.model.domain.RelationshipKey

data class RelationshipDefInMemory(
    override val id: RelationshipKey,
    override val name: LocalizedText?,
    override val description: LocalizedText?,
    override val roles: List<RelationshipRoleInMemory>,
    override val attributes: List<AttributeDefInMemory>,
    override val hashtags: List<Hashtag>
) : RelationshipDef {
    companion object {
        fun of(other: RelationshipDef): RelationshipDefInMemory {
            return RelationshipDefInMemory(
            id = other.id,
                name = other.name,
                description = other.description,
                roles = other.roles.map(RelationshipRoleInMemory::of),
                attributes = other.attributes.map(AttributeDefInMemory::of),
                hashtags = other.hashtags

            )
        }
    }
}
