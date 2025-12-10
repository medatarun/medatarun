package io.medatarun.data.internal

import io.medatarun.data.domain.EntityId
import io.medatarun.data.ports.exposed.DataCmd
import io.medatarun.data.ports.exposed.EntityInitializer
import io.medatarun.data.ports.exposed.EntityUpdater
import io.medatarun.data.ports.needs.DataStorages
import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.ModelId
import io.medatarun.model.ports.exposed.ModelQueries

class DataCmdImpl(val storages: DataStorages, val models: ModelQueries) : DataCmd {
    override fun createEntity(modelId: ModelId, entityDefId: EntityDefId, entityInitializer: EntityInitializer) {
        storages.createEntity(models.findModelById(modelId), entityDefId, entityInitializer)
    }

    override fun updateEntity(modelId: ModelId, entityDefId: EntityDefId, values: EntityUpdater) {
        storages.updateEntity(models.findModelById(modelId), entityDefId, values)
    }

    override fun deleteEntity(modelId: ModelId, entityDefId: EntityDefId, entityId: EntityId) {
        storages.deleteEntity(models.findModelById(modelId), entityDefId, entityId)
    }
}