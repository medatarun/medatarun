package io.medatarun.model.infra

import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelRepository
import io.medatarun.model.ports.ModelRepositoryCmd
import io.medatarun.model.ports.ModelRepositoryId

/**
 * A model repository suitable in memory, mostly used in tests
 */
class ModelRepositoryInMemory(val identifier: String) : ModelRepository {
    val repositoryId = ModelRepositoryId(identifier)

    val models = mutableMapOf<ModelId, ModelInMemory>()

    override fun matchesId(id: ModelRepositoryId): Boolean {
        return id == repositoryId
    }

    override fun findAllModelIds(): List<ModelId> {
        return models.keys.toList()
    }

    override fun findModelByIdOptional(id: ModelId): Model? {
        return models[id]
    }

    override fun createModel(model: Model) {
        models[model.id] = ModelInMemory.of(model)
    }

    private fun updateModel(modelId: ModelId, block: (model: ModelInMemory) -> ModelInMemory) {
        val model = models[modelId] ?: throw ModelRepositoryInMemoryExceptions(modelId)
        models[modelId] = block(model)
    }

    override fun updateModelName(modelId: ModelId, name: LocalizedTextNotLocalized) {
        updateModel(modelId) { it.copy(name = name) }
    }

    override fun updateModelDescription(
        modelId: ModelId,
        description: LocalizedTextNotLocalized?
    ) {
        updateModel(modelId) { it.copy(description = description) }
    }

    override fun updateModelVersion(
        modelId: ModelId,
        version: ModelVersion
    ) {
        updateModel(modelId) { it.copy(version = version) }
    }

    override fun deleteModel(modelId: ModelId) {
        models.remove(modelId)
    }

    override fun createType(modelId: ModelId, initializer: ModelTypeInitializer) {
        updateModel(modelId) {
            it.copy(
                types = it.types + ModelTypeInMemory(
                    id = initializer.id,
                    name = initializer.name,
                    description = initializer.description
                )
            )
        }
    }

    override fun updateType(modelId: ModelId, typeId: ModelTypeId, cmd: ModelTypeUpdateCmd) {
        updateModel(modelId) { m ->
            m.copy(types = m.types.map { type ->
                if (type.id != typeId) type else when (cmd) {
                    is ModelTypeUpdateCmd.Name -> type.copy(name = cmd.value)
                    is ModelTypeUpdateCmd.Description -> type.copy(description = cmd.value)
                }
            })
        }
    }

    override fun deleteType(modelId: ModelId, typeId: ModelTypeId) {
        updateModel(modelId) { m ->
            m.copy(types = m.types.mapNotNull { type ->
                if (type.id != typeId) type else null
            })
        }
    }

    override fun createEntityDef(modelId: ModelId, e: EntityDef) {
        updateModel(modelId) {
            it.copy(entityDefs = it.entityDefs + EntityDefInMemory.of(e))
        }
    }

    fun modifyingEntityDef(modelId: ModelId, e: EntityDefId, block: (EntityDefInMemory) -> EntityDefInMemory?) {
        updateModel(modelId) {
            it.copy(
                entityDefs = it.entityDefs.mapNotNull { entityDef ->
                    if (entityDef.id != e) entityDef else block(entityDef)
                })
        }
    }

    fun modifyingEntityDefAttributeDef(
        modelId: ModelId,
        e: EntityDefId,
        attributeDefId: AttributeDefId,
        block: (AttributeDefInMemory) -> AttributeDefInMemory?
    ) {
        updateModel(modelId) {
            it.copy(
                entityDefs = it.entityDefs.map { entityDef ->
                    if (entityDef.id != e) entityDef else entityDef.copy(
                        attributes = entityDef.attributes.mapNotNull { attr ->
                            if (attr.id != attributeDefId) attr else block(attr)
                        }
                    )
                })
        }
    }


    override fun updateEntityDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        cmd: EntityDefUpdateCmd
    ) {
        modifyingEntityDef(modelId, entityDefId) { previous ->
            when (cmd) {
                is EntityDefUpdateCmd.Id -> previous.copy(id = cmd.value)
                is EntityDefUpdateCmd.Name -> previous.copy(name = cmd.value)
                is EntityDefUpdateCmd.Description -> previous.copy(description = cmd.value)
                is EntityDefUpdateCmd.IdentifierAttribute -> previous.copy(identifierAttributeDefId = cmd.value)
            }
        }
    }

    override fun deleteEntityDef(
        modelId: ModelId,
        entityDefId: EntityDefId
    ) {
        modifyingEntityDef(modelId, entityDefId) { null }
    }

    override fun dispatch(cmd: ModelRepositoryCmd) {
        updateModel(cmd.modelId) { model -> ModelInMemoryReducer().dispatch(model, cmd) }
    }

    /**
     * Pushes a model in the list of known models. Don't check if it's valid or not, on purpose.
     * Very dangerous to use, mostly for tests
     */
    fun push(model: ModelInMemory) {
        this.models[model.id] = model
    }
}

class ModelRepositoryInMemoryExceptions(modelId: ModelId) :
    MedatarunException("Model not found in repository ${modelId.value}")