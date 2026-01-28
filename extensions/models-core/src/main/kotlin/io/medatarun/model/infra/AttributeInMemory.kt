package io.medatarun.model.infra

import io.medatarun.model.domain.*

/**
 * Default implementation of [Attribute]
 */
data class AttributeInMemory(
    override val id: AttributeId,
    override val key: AttributeKey,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val typeId: TypeId,
    override val optional: Boolean,
    override val hashtags: List<Hashtag>
) : Attribute {
    companion object {

        fun of(other: Attribute): AttributeInMemory {
            return AttributeInMemory(
                id = other.id,
                key = other.key,
                name = other.name,
                description = other.description,
                typeId = other.typeId,
                optional = other.optional,
                hashtags = other.hashtags,
            )
        }
    }
}