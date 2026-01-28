package io.medatarun.model.infra

import io.medatarun.model.domain.*

data class RelationshipDefInMemory(
    override val id: RelationshipId,
    override val key: RelationshipKey,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val roles: List<RelationshipRoleInMemory>,
    override val attributes: List<AttributeInMemory>,
    override val hashtags: List<Hashtag>
) : RelationshipDef {
    companion object {
        fun of(other: RelationshipDef): RelationshipDefInMemory {
            return RelationshipDefInMemory(
                id = other.id,
                key = other.key,
                name = other.name,
                description = other.description,
                roles = other.roles.map(RelationshipRoleInMemory::of),
                attributes = other.attributes.map(AttributeInMemory::of),
                hashtags = other.hashtags

            )
        }
    }
}
