package io.medatarun.model.ports.needs

import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.search.SearchResults

/**
 * Model storage allows accessing and modifying stored models.
 *
 * The current runtime uses one native SQL storage selected by the model extension itself.
 */
interface ModelStorage {

    fun findAllModelIds(): List<ModelId>

    fun existsModelByKey(key: ModelKey): Boolean

    fun existsModelById(id: ModelId): Boolean

    fun findModelByKeyOptional(key: ModelKey): Model?

    fun findModelByIdOptional(id: ModelId): Model?

    fun search(query: ModelStorageSearchQuery): SearchResults

    // Commands

    /**
     * Process one model storage command.
     *
     * See [ModelRepoCmd] for the list of supported write operations.
     */
    fun dispatch(cmd: ModelRepoCmd)

}
