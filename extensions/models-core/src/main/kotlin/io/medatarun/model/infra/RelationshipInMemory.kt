package io.medatarun.model.infra

import io.medatarun.model.domain.*

data class RelationshipInMemory(
    override val id: RelationshipId,
    override val key: RelationshipKey,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val roles: List<RelationshipRoleInMemory>,
    override val attributes: List<AttributeInMemory>,
    override val hashtags: List<Hashtag>
) : Relationship {
    companion object {
        fun of(other: Relationship): RelationshipInMemory {
            return RelationshipInMemory(
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
