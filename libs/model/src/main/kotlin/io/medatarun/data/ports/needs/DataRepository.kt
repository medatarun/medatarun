package io.medatarun.data.ports.needs

import io.medatarun.data.domain.Entity
import io.medatarun.data.domain.EntityId
import io.medatarun.data.ports.exposed.EntityInitializer
import io.medatarun.data.ports.exposed.EntityUpdater
import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelId

interface DataRepository {

    fun matches(modelId: ModelId, entityDefId: EntityDefId): Boolean

    /**
     * Lists all [EntityDefId] managed by this repository for specified [modelId]
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