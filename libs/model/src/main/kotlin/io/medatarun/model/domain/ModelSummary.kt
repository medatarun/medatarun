package io.medatarun.model.domain

data class ModelSummary(
    val id: ModelId,
    val name: String?,
    val description: String?,
    val error: String?,
    val countTypes: Int,
    val countEntities: Int,
    val countRelationships: Int
)
