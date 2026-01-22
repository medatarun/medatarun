package io.medatarun.model.infra

import io.medatarun.model.domain.*
import java.net.URL

/**
 * Default implementation of Model
 */
data class ModelInMemory(
    override val key: ModelKey,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val version: ModelVersion,
    override val origin: ModelOrigin,
    override val types: List<ModelTypeInMemory>,
    override val entityDefs: List<EntityDefInMemory>,
    override val relationshipDefs: List<RelationshipDefInMemory>,
    override val documentationHome: URL?,
    override val hashtags: List<Hashtag>,
) : Model {

    companion object {
        fun of(other: Model): ModelInMemory {
            return ModelInMemory(
                key = other.key,
                name = other.name,
                description = other.description,
                version = other.version,
                origin = other.origin,
                types = other.types.map(ModelTypeInMemory::of),
                entityDefs = other.entityDefs.map(EntityDefInMemory::of),
                relationshipDefs = other.relationshipDefs.map(RelationshipDefInMemory::of),
                documentationHome = other.documentationHome,
                hashtags = other.hashtags
            )
        }

        class Builder(
            val id: ModelKey,
            var name: LocalizedText? = null,
            var description: LocalizedMarkdown? = null,
            val version: ModelVersion,
            var origin: ModelOrigin = ModelOrigin.Manual,
            var types: MutableList<ModelTypeInMemory> = mutableListOf(),
            var entityDefs: MutableList<EntityDefInMemory> = mutableListOf(),
            var relationshipDefs: MutableList<RelationshipDefInMemory> = mutableListOf(),
            var documentationHome: URL? = null,
            var hashtags: MutableList<Hashtag> = mutableListOf(),
        ) {
            fun build(): ModelInMemory {
                return ModelInMemory(
                    key = id,
                    name = name,
                    description = description,
                    version = version,
                    origin = origin,
                    types = types,
                    entityDefs = entityDefs,
                    relationshipDefs = relationshipDefs,
                    documentationHome = documentationHome,
                    hashtags = hashtags,
                )
            }

            fun addEntityDef(
                id: EntityKey,
                identifierAttributeKey: AttributeKey,
                block: EntityDefInMemory.Companion.Builder.() -> Unit = {}
            ): EntityDefInMemory {
                val e = EntityDefInMemory.builder(id, identifierAttributeKey, block)
                entityDefs.add(e)
                return e
            }
        }

        fun builder(id: ModelKey, version: ModelVersion, block: Builder.() -> Unit): ModelInMemory {
            return Builder(id = id, version = version).apply(block).build()
        }

    }
}