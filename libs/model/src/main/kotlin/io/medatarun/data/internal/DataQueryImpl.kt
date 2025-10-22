package io.medatarun.data.internal

import io.medatarun.data.model.DataQuery
import io.medatarun.data.model.Entity
import io.medatarun.data.ports.DataStorages
import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelQueries

class DataQueryImpl(val storages: DataStorages, val modelQueries: ModelQueries) : DataQuery {
    override fun findAllEntity(modelId: ModelId, entityDefId: EntityDefId): List<Entity> {
        return storages.findAllEntities(modelQueries.findModelById(modelId), entityDefId)
    }
}