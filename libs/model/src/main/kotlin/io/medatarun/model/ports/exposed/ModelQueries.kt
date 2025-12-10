package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelSummary
import java.util.*

interface ModelQueries {

    /**
     * Find a model by its id or throw [io.medatarun.model.domain.ModelNotFoundException]
     */
    fun findModelById(modelKey: ModelKey): Model

    /**
     * Returns complete list of all known model ids in this application instance
     */
    fun findAllModelIds(): List<ModelKey>
    fun findAllModelSummaries(locale: Locale): List<ModelSummary>
}