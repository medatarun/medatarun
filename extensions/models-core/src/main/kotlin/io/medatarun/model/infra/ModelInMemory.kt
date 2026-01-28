package io.medatarun.model.infra

import io.medatarun.model.domain.*
import java.net.URL

/**
 * Default implementation of Model
 */
data class ModelInMemory(
    override val id: ModelId,
    override val key: ModelKey,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val version: ModelVersion,
    override val origin: ModelOrigin,
    override val types: List<ModelTypeInMemory>,
    override val entities: List<EntityInMemory>,
    override val relationships: List<RelationshipDefInMemory>,
    override val documentationHome: URL?,
    override val hashtags: List<Hashtag>,
) : Model {

    companion object {
        fun of(other: Model): ModelInMemory {
            return ModelInMemory(
                id = other.id,
                key = other.key,
                name = other.name,
                description = other.description,
                version = other.version,
                origin = other.origin,
                types = other.types.map(ModelTypeInMemory::of),
                entities = other.entities.map(EntityInMemory::of),
                relationships = other.relationships.map(RelationshipDefInMemory::of),
                documentationHome = other.documentationHome,
                hashtags = other.hashtags
            )
        }

        class Builder(
            var id: ModelId = ModelId.generate(),
            val key: ModelKey,
            var name: LocalizedText? = null,
            var description: LocalizedMarkdown? = null,
            val version: ModelVersion,
            var origin: ModelOrigin = ModelOrigin.Manual,
            var types: MutableList<ModelTypeInMemory> = mutableListOf(),
            var entities: MutableList<EntityInMemory> = mutableListOf(),
            var relationships: MutableList<RelationshipDefInMemory> = mutableListOf(),
            var documentationHome: URL? = null,
            var hashtags: MutableList<Hashtag> = mutableListOf(),
        ) {
            fun build(): ModelInMemory {
                return ModelInMemory(
                    id = id,
                    key = key,
                    name = name,
                    description = description,
                    version = version,
                    origin = origin,
                    types = types,
                    entities = entities,
                    relationships = relationships,
                    documentationHome = documentationHome,
                    hashtags = hashtags,
                )
            }

            fun addEntity(
                key: EntityKey,
                identifierAttributeId: AttributeId,
                block: EntityInMemory.Companion.Builder.() -> Unit = {}
            ): EntityInMemory {
                val e = EntityInMemory.builder(key, identifierAttributeId, block)
                entities.add(e)
                return e
            }
        }

        fun builder(key: ModelKey, version: ModelVersion, block: Builder.() -> Unit): ModelInMemory {
            return Builder(key = key, version = version).apply(block).build()
        }

    }
}