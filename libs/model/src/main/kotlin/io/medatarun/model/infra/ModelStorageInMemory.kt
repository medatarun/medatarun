package io.medatarun.model.infra

import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelStorage



data class ModelInMemory(
    override val id: ModelId,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val version: ModelVersion,
    override val entities: List<ModelEntityInMemory>
) : Model {

}

data class ModelEntityInMemory(
    override val id: ModelEntityId,
    override val name: LocalizedText?,
    override val attributes: List<ModelAttributeInMemory>,
    override val description: LocalizedMarkdown?,
) : ModelEntity {

    private val map = attributes.associateBy { it.id }

    override fun countAttributes(): Int {
        return attributes.size
    }

    override fun getAttribute(id: ModelAttributeId): ModelAttribute {
        return map[id] ?: throw ModelEntityAttributeNotFoundException(this.id, id)
    }

    override fun hasAttribute(id: ModelAttributeId): Boolean = map.containsKey(id)

    companion object {
        fun of(other: ModelEntity): ModelEntityInMemory {
            return ModelEntityInMemory(
                id = other.id,
                name = other.name,
                description = other.description,
                attributes = other.attributes.map(ModelAttributeInMemory::of),
            )
        }
    }
}

data class ModelAttributeInMemory(
    override val id: ModelAttributeId,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val type: ModelTypeId,
    override val optional: Boolean
) : ModelAttribute {
    companion object {
        fun of(other: ModelAttribute): ModelAttributeInMemory {
            return ModelAttributeInMemory(
                id = other.id,
                name = other.name,
                description = other.description,
                type = other.type,
                optional = other.optional
            )
        }
    }
}
