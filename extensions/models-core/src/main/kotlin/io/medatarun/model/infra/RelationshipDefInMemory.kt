package io.medatarun.model.infra

import io.medatarun.model.domain.*

data class RelationshipDefInMemory(
    override val id: RelationshipKey,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
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
