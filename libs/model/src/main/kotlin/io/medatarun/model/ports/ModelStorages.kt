package io.medatarun.model.ports

import io.medatarun.model.model.*

/**
 * Represents an aggregation of multiple repositories for models.
 *
 * Do not confuse with [ModelRepository] which represents only ONE repository at a time.
 *
 * Implementations shall be able to aggregate multiple repositories as declared in the contribution point for repositories.
 *
 * [ModelStorages] contains multiple [ModelRepository]
 */
interface ModelStorages {

    // Models

    fun findAllModelIds(): List<ModelId>
    fun findModelById(id: ModelId): Model
    fun findModelByIdOptional(modelId: ModelId): Model?
    fun existsModelById(modelId: ModelId): Boolean
    fun createModel(model: Model, repositoryRef: RepositoryRef)
    fun updateModelName(modelId: ModelId, name: LocalizedTextNotLocalized)
    fun updateModelDescription(modelId: ModelId, description: LocalizedTextNotLocalized?)
    fun updateModelVersion(modelId: ModelId, version: ModelVersion)
    fun deleteModel(modelId: ModelId)

    // Models -> EntityDef

    //@formatter:off
    fun createEntityDef(modelId: ModelId, e: EntityDef)
    fun updateEntityDef(modelId: ModelId, entityDefId: EntityDefId, cmd: EntityDefUpdateCmd)
    fun deleteEntityDef(modelId: ModelId, entityDefId: EntityDefId)
    //@formatter:on

    // Model -> EntityDef -> AttributeDef

    //@formatter:off
    fun createEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attr: AttributeDef)
    fun updateEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, cmd: AttributeDefUpdateCmd)
    fun deleteEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId)
    //@formatter:on
}
