package io.medatarun.model.model

data class ModelTypeInitializer(
    val id: ModelTypeId,
    val name: LocalizedText?,
    val description: LocalizedText?
)
