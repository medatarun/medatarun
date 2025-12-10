package io.medatarun.data.ports.needs

import io.medatarun.data.domain.Entity
import io.medatarun.data.domain.EntityId
import io.medatarun.data.ports.exposed.EntityInitializer
import io.medatarun.data.ports.exposed.EntityUpdater
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.Model

interface DataStorages {
    fun findAllEntities(model: Model, entityKey: EntityKey): List<Entity>
    fun createEntity(model: Model, entityKey: EntityKey, entityInitializer: EntityInitializer)
    fun updateEntity(model: Model, entityKey: EntityKey, entityUpdater: EntityUpdater)
    fun deleteEntity(model: Model, entityKey: EntityKey, entityId: EntityId)
}