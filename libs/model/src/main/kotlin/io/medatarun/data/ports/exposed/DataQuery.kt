package io.medatarun.data.ports.exposed

import io.medatarun.data.domain.Entity
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.ModelKey

interface DataQuery {
    fun findAllEntity(modelKey: ModelKey, entityKey: EntityKey): List<Entity>
}