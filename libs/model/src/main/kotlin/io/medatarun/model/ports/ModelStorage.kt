package io.medatarun.model.ports

import io.medatarun.model.model.Model
import io.medatarun.model.model.ModelId

interface ModelStorage {
    fun findById(id: ModelId): Model
}