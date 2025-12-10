package io.medatarun.data.adapters

import io.medatarun.data.model.*
import io.medatarun.data.ports.DataRepository
import io.medatarun.data.ports.DataStorages
import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelId

class DataStoragesComposite(val repositories: List<DataRepository>): DataStorages {

    override fun findAllEntities(
        model: Model,
        entityDefId: EntityDefId
    ): List<Entity> {
        val repo = findRepo(model.id, entityDefId)
        return repo.findAllEntities(model, entityDefId)
    }

    override fun createEntity(
        model: Model,
        entityDefId: EntityDefId,
        entityInitializer: EntityInitializer
    ) {
        val repo = findRepo(model.id, entityDefId)
        return repo.createEntity(model, entityDefId, entityInitializer)
    }

    override fun updateEntity(
        model: Model,
        entityDefId: EntityDefId,
        entityUpdater: EntityUpdater
    ) {
        val repo = findRepo(model.id, entityDefId)
        return repo.updateEntity(model, entityDefId, entityUpdater)
    }

    override fun deleteEntity(
        model: Model,
        entityDefId: EntityDefId,
        entityId: EntityId
    ) {
        val repo = findRepo(model.id, entityDefId)
        return repo.deleteEntity(model, entityDefId, entityId)
    }

    fun findRepo(modelId: ModelId, entityDefId: EntityDefId): DataRepository {
        return repositories.firstOrNull {
            it.matches(modelId, entityDefId)
        } ?: throw DataStorageNotFoundException(modelId, entityDefId)
    }
}