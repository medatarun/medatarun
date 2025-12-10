package io.medatarun.data.internal

import io.medatarun.data.domain.EntityId
import io.medatarun.data.ports.exposed.DataCmd
import io.medatarun.data.ports.exposed.EntityInitializer
import io.medatarun.data.ports.exposed.EntityUpdater
import io.medatarun.data.ports.needs.DataStorages
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.ports.exposed.ModelQueries

class DataCmdImpl(val storages: DataStorages, val models: ModelQueries) : DataCmd {
    override fun createEntity(modelKey: ModelKey, entityKey: EntityKey, entityInitializer: EntityInitializer) {
        storages.createEntity(models.findModelById(modelKey), entityKey, entityInitializer)
    }

    override fun updateEntity(modelKey: ModelKey, entityKey: EntityKey, values: EntityUpdater) {
        storages.updateEntity(models.findModelById(modelKey), entityKey, values)
    }

    override fun deleteEntity(modelKey: ModelKey, entityKey: EntityKey, entityId: EntityId) {
        storages.deleteEntity(models.findModelById(modelKey), entityKey, entityId)
    }
}