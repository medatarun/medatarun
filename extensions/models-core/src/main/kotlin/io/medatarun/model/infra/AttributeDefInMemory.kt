package io.medatarun.model.infra

import io.medatarun.model.domain.*

/**
 * Default implementation of AttributeDef
 */
data class AttributeDefInMemory(
    override val key: AttributeKey,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val type: TypeKey,
    override val optional: Boolean,
    override val hashtags: List<Hashtag>
) : AttributeDef {
    companion object {
        fun of(other: AttributeDef): AttributeDefInMemory {
            return AttributeDefInMemory(
                key = other.key,
                name = other.name,
                description = other.description,
                type = other.type,
                optional = other.optional,
                hashtags = other.hashtags,
            )
        }
    }
}