package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.*
import java.util.*

interface ModelQueries {

    /**
     * Find a model by its id or throw [io.medatarun.model.domain.ModelNotFoundByKeyException]
     */
    fun findModelByKey(modelKey: ModelKey): Model
    fun findModelById(modelId: ModelId): Model
    fun findModelByRef(modelRef: ModelRef): Model

    /**
     * Returns complete list of all known model ids in this application instance
     */
    fun findAllModelIds(): List<ModelId>
    fun findAllModelSummaries(locale: Locale): List<ModelSummary>
}