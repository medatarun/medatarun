package io.medatarun.model.ports.needs

import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey

/**
 * Model storage allows accessing and modifying stored models.
 *
 * The current runtime uses one native SQL storage selected by the model extension itself.
 */
interface ModelStorage {

    // Queries

    /**
     * Returns true if the storage matches this id.
     *
     * Storage ids stay in the API because callers can still name the target storage explicitly.
     * The current SQL runtime exposes a single storage, but the id contract remains part of the port.
     */
    fun matchesId(id: ModelRepositoryId): Boolean

    fun findAllModelIds(): List<ModelId>

    fun existsModelByKey(key: ModelKey): Boolean

    fun existsModelById(id: ModelId): Boolean

    fun findModelByKeyOptional(key: ModelKey): Model?

    fun findModelByIdOptional(id: ModelId): Model?

    // Commands

    /**
     * Process this command. See [io.medatarun.model.ports. in.ModelRepositoryCmd] to have the list of all available commands to implement
     * to have a compatible repository.
     */
    fun dispatch(cmd: ModelRepoCmd)

}
