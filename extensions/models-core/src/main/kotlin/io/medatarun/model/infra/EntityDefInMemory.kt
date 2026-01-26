package io.medatarun.model.infra

import io.medatarun.model.domain.*
import java.net.URL

/**
 * Default implementation of EntityDef
 */
data class EntityDefInMemory(
    override val id: EntityId,
    override val key: EntityKey,
    override val name: LocalizedText?,
    override val attributes: List<AttributeDefInMemory>,
    override val description: LocalizedMarkdown?,
    override val identifierAttributeKey: AttributeKey,
    override val origin: EntityOrigin,
    override val documentationHome: URL?,
    override val hashtags: List<Hashtag>,
) : EntityDef {

    private val map = attributes.associateBy { it.key }

    fun countAttributeDefs(): Int {
        return attributes.size
    }

    override fun getAttributeDefOptional(id: AttributeKey): AttributeDef? {
        return map[id]
    }




    companion object {
        fun of(other: EntityDef): EntityDefInMemory {
            return EntityDefInMemory(
                id = other.id,
                key = other.key,
                name = other.name,
                description = other.description,
                attributes = other.attributes.map(AttributeDefInMemory::of),
                identifierAttributeKey = other.identifierAttributeKey,
                origin = other.origin,
                documentationHome = other.documentationHome,
                hashtags = other.hashtags
            )
        }

        class Builder(
            val id: EntityId = EntityId.generate(),
            val key: EntityKey,
            var name: LocalizedText? = null,
            var attributes: MutableList<AttributeDefInMemory> = mutableListOf(),
            var description: LocalizedMarkdown? = null,
            var identifierAttributeKey: AttributeKey,
            var origin: EntityOrigin = EntityOrigin.Manual,
            var documentationHome: URL? = null,
            var hashtags: MutableList<Hashtag> = mutableListOf(),
        ) {

            fun addAttribute(vararg attributes: AttributeDefInMemory) {
                this.attributes.addAll(attributes)
            }

            fun build(): EntityDefInMemory {
                return EntityDefInMemory(
                    id = id,
                    key = key,
                    name = name,
                    attributes = attributes,
                    description = description,
                    identifierAttributeKey = identifierAttributeKey,
                    origin = origin,
                    documentationHome = documentationHome,
                    hashtags = hashtags
                )
            }
        }

        fun builder(
            key: EntityKey,
            identifierAttributeKey: AttributeKey,
            block: Builder.() -> Unit = {}
        ): EntityDefInMemory {
            return Builder(key = key, identifierAttributeKey = identifierAttributeKey).also(block).build()
        }
    }
}