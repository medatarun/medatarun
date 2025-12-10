package io.medatarun.data.internal

import io.medatarun.data.domain.Entity
import io.medatarun.data.ports.exposed.DataQuery
import io.medatarun.data.ports.needs.DataStorages
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.ports.exposed.ModelQueries

class DataQueryImpl(val storages: DataStorages, val modelQueries: ModelQueries) : DataQuery {
    override fun findAllEntity(modelKey: ModelKey, entityKey: EntityKey): List<Entity> {
        return storages.findAllEntities(modelQueries.findModelById(modelKey), entityKey)
    }
}