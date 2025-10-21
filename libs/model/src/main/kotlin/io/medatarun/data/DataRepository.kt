package io.medatarun.data

import io.medatarun.model.model.Model
import io.medatarun.model.model.ModelEntityId
import io.medatarun.model.model.ModelId

interface DataRepository {
    /**
     * Lists all EntityType managed by this repository for specified [modelId]
     */
    fun managedEntityDefs(modelId: ModelId): Set<ModelEntityId>
    /**
     * Lists all EntityInstance managed by this
     */
    fun findAllEntities(model: Model, entityDefId: ModelEntityId): List<Entity>
    fun createEntity(model: Model, entityDefId: ModelEntityId, entityInitializer: EntityInitializer)
    fun updateEntity(model: Model, entityDefId: ModelEntityId, entityUpdater: EntityUpdater)
    fun deleteEntity(model: Model, entityDefId: ModelEntityId, entityId: EntityInstanceId)
}