package io.medatarun.model.ports.needs

import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelAggregate
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelType
import io.medatarun.model.domain.TypeId
import io.medatarun.model.domain.TypeKey
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

    fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate?

    fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate?

    fun findTypeByKeyOptional(modelId: ModelId, key: TypeKey): ModelType?

    fun findTypeByIdOptional(modelId: ModelId, typeId: TypeId): ModelType?

    fun search(query: ModelStorageSearchQuery): SearchResults

    fun isTypeUsedInEntityAttributes(modelId: ModelId, typeId: TypeId): Boolean
    fun isTypeUsedInRelationshipAttributes(modelId: ModelId, typeId: TypeId): Boolean

    // Commands

    /**
     * Process one model storage command.
     *
     * See [ModelRepoCmd] for the list of supported write operations.
     */
    fun dispatch(cmd: ModelRepoCmd)



}
