package io.medatarun.data

import io.medatarun.model.model.Model
import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.ModelId

interface DataRepository {
    /**
     * Lists all EntityType managed by this repository for specified [modelId]
     */
    fun managedEntityDefs(modelId: ModelId): Set<EntityDefId>
    /**
     * Lists all EntityInstance managed by this
     */
    fun findAllEntities(model: Model, entityDefId: EntityDefId): List<Entity>
    fun createEntity(model: Model, entityDefId: EntityDefId, entityInitializer: EntityInitializer)
    fun updateEntity(model: Model, entityDefId: EntityDefId, entityUpdater: EntityUpdater)
    fun deleteEntity(model: Model, entityDefId: EntityDefId, entityId: EntityInstanceId)
}