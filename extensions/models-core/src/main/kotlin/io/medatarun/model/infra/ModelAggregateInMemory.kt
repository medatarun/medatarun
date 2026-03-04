package io.medatarun.model.infra

import io.medatarun.model.domain.*
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.tags.core.domain.TagId
import java.net.URL

/**
 * Default implementation of Model
 */
data class ModelAggregateInMemory(
    override val model: ModelInMemory,
    override val types: List<ModelTypeInMemory>,
    override val entities: List<EntityInMemory>,
    override val relationships: List<RelationshipInMemory>,
    override val tags: List<TagId>,
) : ModelAggregate, Model by model {

    companion object {
        fun of(other: ModelAggregate): ModelAggregateInMemory {
            return ModelAggregateInMemory(
                model = ModelInMemory.of(other),
                types = other.types.map(ModelTypeInMemory::of),
                entities = other.entities.map(EntityInMemory::of),
                relationships = other.relationships.map(RelationshipInMemory::of),
                tags = other.tags,
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
            var relationships: MutableList<RelationshipInMemory> = mutableListOf(),
            var documentationHome: URL? = null,
            var tags: MutableList<TagId> = mutableListOf(),
        ) {
            fun build(): ModelAggregateInMemory {
                return ModelAggregateInMemory(
                    model = ModelInMemory(
                        id = id,
                        key = key,
                        name = name,
                        description = description,
                        version = version,
                        origin = origin,
                        documentationHome = documentationHome,
                    ),
                    types = types,
                    entities = entities,
                    relationships = relationships,
                    tags = tags,
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

        fun builder(key: ModelKey, version: ModelVersion, block: Builder.() -> Unit): ModelAggregateInMemory {
            return Builder(key = key, version = version).apply(block).build()
        }

    }
}
