package io.medatarun.model.model

import io.medatarun.model.ports.RepositoryRef

/**
 * Commands to change the model, entity definitions, entity definition's attributes definitions
 */
interface ModelCmd {

    // Model

    fun createModel(id: ModelId, name: LocalizedText, description: LocalizedMarkdown?, version: ModelVersion, repositoryRef: RepositoryRef = RepositoryRef.Auto)
    fun updateModelName(modelId: ModelId, name: LocalizedTextNotLocalized)
    fun updateModelDescription(modelId: ModelId, description: LocalizedTextNotLocalized?)
    fun updateModelVersion(modelId: ModelId, version: ModelVersion)
    fun deleteModel(modelId: ModelId)

    // Model -> EntityDef

    fun createEntityDef(modelId: ModelId, entityDefInitializer: EntityDefInitializer)
    fun updateEntityDef(modelId: ModelId, entityDefId: EntityDefId, cmd: EntityDefUpdateCmd)
    fun deleteEntityDef(modelId: ModelId, entityDefId: EntityDefId)

    // Model -> EntityDef -> AttributeDef

    fun createEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefInitializer: AttributeDefInitializer)
    fun deleteEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId)
    fun updateEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, target: AttributeDefUpdateCmd)
}
