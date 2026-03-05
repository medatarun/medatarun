package io.medatarun.model.infra

import io.medatarun.model.domain.*
import io.medatarun.tags.core.domain.TagId
import java.net.URL

/**
 * Default implementation of [Entity]
 */
data class EntityInMemory(
    override val id: EntityId,
    override val key: EntityKey,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val identifierAttributeId: AttributeId,
    override val origin: EntityOrigin,
    override val documentationHome: URL?,
    override val tags: List<TagId>,
) : Entity {

    companion object {
        fun of(other: Entity): EntityInMemory {
            return EntityInMemory(
                id = other.id,
                key = other.key,
                name = other.name,
                description = other.description,
                identifierAttributeId = other.identifierAttributeId,
                origin = other.origin,
                documentationHome = other.documentationHome,
                tags = other.tags,
            )
        }

        class Builder(
            val id: EntityId = EntityId.generate(),
            val key: EntityKey,
            var name: LocalizedText? = null,
            var description: LocalizedMarkdown? = null,
            var identifierAttributeId: AttributeId,
            var origin: EntityOrigin = EntityOrigin.Manual,
            var documentationHome: URL? = null,
            var tags: MutableList<TagId> = mutableListOf(),
        ) {

            fun build(): EntityInMemory {
                return EntityInMemory(
                    id = id,
                    key = key,
                    name = name,
                    description = description,
                    identifierAttributeId = identifierAttributeId,
                    origin = origin,
                    documentationHome = documentationHome,
                    tags = tags,
                )
            }
        }

        fun builder(
            key: EntityKey,
            identifierAttributeId: AttributeId,
            block: Builder.() -> Unit = {}
        ): EntityInMemory {
            return Builder(key = key, identifierAttributeId = identifierAttributeId).also(block).build()
        }
    }
}
