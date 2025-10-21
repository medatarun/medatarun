package io.medatarun.model.infra

import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelRepository
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
        val model = models[modelId] ?: throw ModelRepositoryInMemoryModelNotFoundException(modelId)
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


    override fun updateEntityDefId(
        modelId: ModelId,
        entityDefId: EntityDefId,
        newEntityDefId: EntityDefId
    ) {
        modifyingEntityDef(modelId, entityDefId) { previous ->
            previous.copy(id = newEntityDefId)
        }
    }

    override fun updateEntityDefName(
        modelId: ModelId,
        entityDefId: EntityDefId,
        name: LocalizedText?
    ) {
        modifyingEntityDef(modelId, entityDefId) { previous ->
            previous.copy(name = name)
        }
    }

    override fun updateEntityDefDescription(
        modelId: ModelId,
        entityDefId: EntityDefId,
        description: LocalizedMarkdown?
    ) {
        modifyingEntityDef(modelId, entityDefId) { previous ->
            previous.copy(description = description)
        }
    }

    override fun deleteEntityDef(
        modelId: ModelId,
        entityDefId: EntityDefId
    ) {
        modifyingEntityDef(modelId, entityDefId) { null }
    }

    override fun createEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attr: AttributeDef
    ) {
        modifyingEntityDef(modelId, entityDefId) {
            it.copy(attributes = it.attributes + AttributeDefInMemory.of(attr))
        }
    }

    override fun updateEntityDefAttributeDefId(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        newAttributeDefId: AttributeDefId
    ) {
        modifyingEntityDefAttributeDef(modelId, entityDefId, attributeDefId) { a ->
            a.copy(id = attributeDefId)
        }
    }

    override fun updateEntityDefAttributeDefName(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        name: LocalizedText?
    ) {
        modifyingEntityDefAttributeDef(modelId, entityDefId, attributeDefId) { a ->
            a.copy(name = name)
        }
    }

    override fun updateEntityDefAttributeDefDescription(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        description: LocalizedMarkdown?
    ) {
        modifyingEntityDefAttributeDef(modelId, entityDefId, attributeDefId) { a ->
            a.copy(description = description)
        }
    }

    override fun updateEntityDefAttributeDefType(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        type: ModelTypeId
    ) {
        modifyingEntityDefAttributeDef(modelId, entityDefId, attributeDefId) { a ->
            a.copy(type = type)
        }
    }

    override fun updateEntityDefAttributeDefOptional(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        optional: Boolean
    ) {
        modifyingEntityDefAttributeDef(modelId, entityDefId, attributeDefId) { a ->
            a.copy(optional = optional)
        }
    }

    override fun deleteEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId
    ) {
        modifyingEntityDefAttributeDef(modelId, entityDefId, attributeDefId) { null }
    }
}