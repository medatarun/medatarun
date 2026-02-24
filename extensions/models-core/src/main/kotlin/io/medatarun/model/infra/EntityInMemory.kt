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
    override val attributes: List<AttributeInMemory>,
    override val description: LocalizedMarkdown?,
    override val identifierAttributeId: AttributeId,
    override val origin: EntityOrigin,
    override val documentationHome: URL?,
    override val hashtags: List<Hashtag>,
    override val tags: List<TagId> = emptyList(),
) : Entity {

    private val map = attributes.associateBy { it.key }



    fun countAttributes(): Int {
        return attributes.size
    }


    companion object {
        fun of(other: Entity): EntityInMemory {
            return EntityInMemory(
                id = other.id,
                key = other.key,
                name = other.name,
                description = other.description,
                attributes = other.attributes.map(AttributeInMemory::of),
                identifierAttributeId = other.identifierAttributeId,
                origin = other.origin,
                documentationHome = other.documentationHome,
                hashtags = other.hashtags,
                tags = other.tags,
            )
        }

        class Builder(
            val id: EntityId = EntityId.generate(),
            val key: EntityKey,
            var name: LocalizedText? = null,
            var attributes: MutableList<AttributeInMemory> = mutableListOf(),
            var description: LocalizedMarkdown? = null,
            var identifierAttributeId: AttributeId,
            var origin: EntityOrigin = EntityOrigin.Manual,
            var documentationHome: URL? = null,
            var hashtags: MutableList<Hashtag> = mutableListOf(),
            var tags: MutableList<TagId> = mutableListOf(),
        ) {

            fun addAttribute(vararg attributes: AttributeInMemory) {
                this.attributes.addAll(attributes)
            }

            fun build(): EntityInMemory {
                return EntityInMemory(
                    id = id,
                    key = key,
                    name = name,
                    attributes = attributes,
                    description = description,
                    identifierAttributeId = identifierAttributeId,
                    origin = origin,
                    documentationHome = documentationHome,
                    hashtags = hashtags,
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
