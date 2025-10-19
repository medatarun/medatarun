package io.medatarun.model.infra

import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelStorage

class ModelStorageInMemory : ModelStorage {
    val models = mutableListOf<ModelInMemory>()
    override fun findById(id: ModelId): Model {
        return models.first { model -> model.id == id } ?: throw ModelNotFoundException(id)
    }

}

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
}

data class ModelAttributeInMemory(
    override val id: ModelAttributeId,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val type: ModelTypeId,
    override val optional: Boolean
) : ModelAttribute
