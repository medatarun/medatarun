package io.medatarun.data.model

import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.ModelId

interface DataCmd {
    fun createEntity(modelId: ModelId, entityDefId: EntityDefId, entityInitializer: EntityInitializer)
    fun updateEntity(modelId: ModelId, entityDefId: EntityDefId, values: EntityUpdater)
    fun deleteEntity(modelId: ModelId, entityDefId: EntityDefId, entityId: EntityId)
}