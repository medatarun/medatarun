package io.medatarun.data.ports

import io.medatarun.data.model.Entity
import io.medatarun.data.model.EntityId
import io.medatarun.data.model.EntityInitializer
import io.medatarun.data.model.EntityUpdater
import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelId

interface DataRepository {

    fun matches(modelId: ModelId, entityDefId: EntityDefId): Boolean

    /**
     * Lists all [io.medatarun.model.domain.EntityDefId] managed by this repository for specified [modelId]
     */
    fun managedEntityDefs(modelId: ModelId): Set<EntityDefId>
    /**
     * Lists all EntityInstance managed by this
     */
    fun findAllEntities(model: Model, entityDefId: EntityDefId): List<Entity>
    fun createEntity(model: Model, entityDefId: EntityDefId, entityInitializer: EntityInitializer)
    fun updateEntity(model: Model, entityDefId: EntityDefId, entityUpdater: EntityUpdater)
    fun deleteEntity(model: Model, entityDefId: EntityDefId, entityId: EntityId)
}