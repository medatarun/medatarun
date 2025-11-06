package io.medatarun.model.model

interface ModelQueries {

    /**
     * Find a model by its id or throw [ModelNotFoundException]
     */
    fun findModelById(modelId: ModelId): Model

    /**
     * Returns complete list of all known model ids in this application instance
     */
    fun findAllModelIds(): List<ModelId>
    fun findAllModelSummaries(): List<ModelSummary>
}