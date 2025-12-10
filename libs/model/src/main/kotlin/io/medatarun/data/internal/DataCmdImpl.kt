package io.medatarun.data.internal

import io.medatarun.data.model.DataCmd
import io.medatarun.data.model.EntityId
import io.medatarun.data.model.EntityInitializer
import io.medatarun.data.model.EntityUpdater
import io.medatarun.data.ports.DataStorages
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