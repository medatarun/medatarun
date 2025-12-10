package io.medatarun.data.adapters

import io.medatarun.data.domain.DataStorageNotFoundException
import io.medatarun.data.domain.Entity
import io.medatarun.data.domain.EntityId
import io.medatarun.data.ports.exposed.EntityInitializer
import io.medatarun.data.ports.exposed.EntityUpdater
import io.medatarun.data.ports.needs.DataRepository
import io.medatarun.data.ports.needs.DataStorages
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelKey

class DataStoragesComposite(val repositories: List<DataRepository>): DataStorages {

    override fun findAllEntities(
        model: Model,
        entityKey: EntityKey
    ): List<Entity> {
        val repo = findRepo(model.id, entityKey)
        return repo.findAllEntities(model, entityKey)
    }

    override fun createEntity(
        model: Model,
        entityKey: EntityKey,
        entityInitializer: EntityInitializer
    ) {
        val repo = findRepo(model.id, entityKey)
        return repo.createEntity(model, entityKey, entityInitializer)
    }

    override fun updateEntity(
        model: Model,
        entityKey: EntityKey,
        entityUpdater: EntityUpdater
    ) {
        val repo = findRepo(model.id, entityKey)
        return repo.updateEntity(model, entityKey, entityUpdater)
    }

    override fun deleteEntity(
        model: Model,
        entityKey: EntityKey,
        entityId: EntityId
    ) {
        val repo = findRepo(model.id, entityKey)
        return repo.deleteEntity(model, entityKey, entityId)
    }

    fun findRepo(modelKey: ModelKey, entityKey: EntityKey): DataRepository {
        return repositories.firstOrNull {
            it.matches(modelKey, entityKey)
        } ?: throw DataStorageNotFoundException(modelKey, entityKey)
    }
}