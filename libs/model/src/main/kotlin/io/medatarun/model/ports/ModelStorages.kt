package io.medatarun.model.ports

import io.medatarun.model.model.Model
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelInvalidException

/**
 * Represents an aggregation of multiple repositories for models.
 *
 * Do not confuse with [ModelRepository] which represents only ONE repository at a time.
 *
 * Implementations shall be able to aggregate multiple repositories as declared in the contribution point for repositories.
 *
 * [ModelStorages] contains multiple [ModelRepository]
 *
 * [ModelStorages] role is also to be sure that no corrupted data shall enter the main business area.
 * Invalid [Model] shall not be loaded by any means, and throw [ModelInvalidException]
 */
interface ModelStorages {

    // Queries

    fun findAllModelIds(): List<ModelId>
    fun findModelById(id: ModelId): Model
    fun findModelByIdOptional(modelId: ModelId): Model?
    fun existsModelById(modelId: ModelId): Boolean


    // Commands

    fun dispatch(cmd: ModelRepositoryCmd, repositoryRef: RepositoryRef = RepositoryRef.Auto)

}
