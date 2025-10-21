package io.medatarun.model.internal

import io.medatarun.model.model.Model
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelQueries
import io.medatarun.model.ports.ModelStorages

class ModelQueriesImpl(private val storage: ModelStorages) : ModelQueries {
    override fun findAllModelIds(): List<ModelId> {
        return storage.findAllModelIds()
    }

    override fun findModelById(modelId: ModelId): Model {
        return storage.findModelById(modelId)
    }
}