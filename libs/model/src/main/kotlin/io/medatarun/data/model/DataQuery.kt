package io.medatarun.data.model

import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.ModelId

interface DataQuery {
    fun findAllEntity(modelId: ModelId, entityDefId: EntityDefId): List<Entity>
}