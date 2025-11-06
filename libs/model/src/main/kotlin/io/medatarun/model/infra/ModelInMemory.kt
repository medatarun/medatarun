package io.medatarun.model.infra

import io.medatarun.model.model.*
import java.net.URL

/**
 * Default implementation of Model
 */
data class ModelInMemory(
    override val id: ModelId,
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
                id = other.id,
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
            val id: ModelId,
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
                    id = id,
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
                id: EntityDefId,
                identifierAttributeDefId: AttributeDefId,
                block: EntityDefInMemory.Companion.Builder.() -> Unit = {}
            ): EntityDefInMemory {
                val e = EntityDefInMemory.builder(id, identifierAttributeDefId, block)
                entityDefs.add(e)
                return e
            }
        }

        fun builder(id: ModelId, version: ModelVersion, block: Builder.() -> Unit): ModelInMemory {
            return Builder(id = id, version = version).apply(block).build()
        }

    }
}