package io.medatarun.data.model

import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.ModelId

interface DataCmd {
    fun createEntity(modelId: ModelId, entityDefId: EntityDefId, entityInitializer: EntityInitializer)
    fun updateEntity(modelId: ModelId, entityDefId: EntityDefId, values: EntityUpdater)
    fun deleteEntity(modelId: ModelId, entityDefId: EntityDefId, entityId: EntityId)
}