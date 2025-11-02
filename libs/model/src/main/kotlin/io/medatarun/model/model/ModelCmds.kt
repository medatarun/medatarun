package io.medatarun.model.model

import io.medatarun.model.ports.RepositoryRef

/**
 * Commands to change the model, entity definitions, entity definition's attributes definitions
 */
interface ModelCmds {

    // Model

    fun createModel(id: ModelId, name: LocalizedText, description: LocalizedMarkdown?, version: ModelVersion, repositoryRef: RepositoryRef = RepositoryRef.Auto)
    fun importModel(model: Model, repositoryRef: RepositoryRef = RepositoryRef.Auto)
    fun updateModelName(modelId: ModelId, name: LocalizedTextNotLocalized)
    fun updateModelDescription(modelId: ModelId, description: LocalizedTextNotLocalized?)
    fun updateModelVersion(modelId: ModelId, version: ModelVersion)
    fun deleteModel(modelId: ModelId)

    fun dispatch(cmd: ModelCmd)



}
