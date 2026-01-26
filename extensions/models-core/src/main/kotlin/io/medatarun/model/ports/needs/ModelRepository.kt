package io.medatarun.model.ports.needs

import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey

/**
 * Model repository allow accessing and modifying stored models.
 *
 * This is the one extensions should implement to offer model storage capabilities.
 *
 * Do not confuse it with [ModelStorages] that acts as a composite around all [ModelRepository]
 *
 * [ModelStorages] contains multiple [ModelRepository]
 *
 */
interface ModelRepository {

    // Queries

    /**
     * Returns true if the repository matches this id.
     *
     * Why we do this like that: because a repository may have sub repositories (or not).
     * Repository ids can have subpaths, allowing one repository to spread its content into
     * multiple locations (or aggregate them).
     *
     * By not just checking the "id" of the repository but asking if this id matches him,
     * we get a clear answer if we should go with it or not (when creating models for example).
     */
    fun matchesId(id: ModelRepositoryId): Boolean

    fun findAllModelIds(): List<ModelId>

    fun findModelByKeyOptional(key: ModelKey): Model?

    fun findModelByIdOptional(id: ModelId): Model?

    // Commands

    /**
     * Process this command. See [io.medatarun.model.ports. in.ModelRepositoryCmd] to have the list of all available commands to implement
     * to have a compatible repository.
     */
    fun dispatch(cmd: ModelRepositoryCmd)

}