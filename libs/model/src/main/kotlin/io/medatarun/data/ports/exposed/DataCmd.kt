package io.medatarun.data.ports.exposed

import io.medatarun.data.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.ModelKey

interface DataCmd {
    fun createEntity(modelKey: ModelKey, entityKey: EntityKey, entityInitializer: EntityInitializer)
    fun updateEntity(modelKey: ModelKey, entityKey: EntityKey, values: EntityUpdater)
    fun deleteEntity(modelKey: ModelKey, entityKey: EntityKey, entityId: EntityId)
}