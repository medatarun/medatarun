package io.medatarun.data.ports.exposed

import io.medatarun.data.domain.Entity
import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.ModelId

interface DataQuery {
    fun findAllEntity(modelId: ModelId, entityDefId: EntityDefId): List<Entity>
}