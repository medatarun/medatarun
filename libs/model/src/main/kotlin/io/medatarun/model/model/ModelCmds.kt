package io.medatarun.model.model

import io.medatarun.model.ports.RepositoryRef

/**
 * Commands to change the model, entity definitions, entity definition's attributes definitions
 */
interface ModelCmds {

    // Model

    fun importModel(model: Model, repositoryRef: RepositoryRef = RepositoryRef.Auto)

    fun dispatch(cmd: ModelCmd)


}
