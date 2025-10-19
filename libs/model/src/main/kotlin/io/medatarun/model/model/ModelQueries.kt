package io.medatarun.model.model

interface ModelQueries {
    fun findById(modelId: ModelId): Model
    fun findAllIds(): List<ModelId>
}