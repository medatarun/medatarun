package io.medatarun.data.model

import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.ModelId

interface DataQuery {
    fun findAllEntity(modelId: ModelId, entityDefId: EntityDefId): List<Entity>
}