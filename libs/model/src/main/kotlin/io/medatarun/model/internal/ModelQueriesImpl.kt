package io.medatarun.model.internal

import io.medatarun.model.model.Model
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelQueries
import io.medatarun.model.ports.ModelStorage

class ModelQueriesImpl(private val storage: ModelStorage) : ModelQueries {
    override fun findAllIds(): List<ModelId> {
        return storage.findAllModelIds()
    }

    override fun findById(modelId: ModelId): Model {
        return storage.findModelById(modelId)
    }
}