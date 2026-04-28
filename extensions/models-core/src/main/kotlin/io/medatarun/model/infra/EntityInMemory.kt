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
    override val name: TextSingleLine?,
    override val description: TextMarkdown?,
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
                origin = other.origin,
                documentationHome = other.documentationHome,
                tags = other.tags,
            )
        }

        class Builder(
            var id: EntityId = EntityId.generate(),
            var key: EntityKey,
            var name: TextSingleLine? = null,
            var description: TextMarkdown? = null,
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
                    origin = origin,
                    documentationHome = documentationHome,
                    tags = tags,
                )
            }
        }

        fun builder(
            key: EntityKey,
            block: Builder.() -> Unit = {}
        ): EntityInMemory {
            return Builder(key = key).also(block).build()
        }
    }
}
