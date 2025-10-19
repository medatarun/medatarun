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
    override val entities: List<ModelEntityInMemory>
) : Model {

}

data class ModelEntityInMemory(override val id: String, override val name: LocalizedText) : ModelEntity {

}

data class LocalizedTextNotLocalized(override val name: String) : LocalizedText {
    override fun get(locale: String): String = name
}