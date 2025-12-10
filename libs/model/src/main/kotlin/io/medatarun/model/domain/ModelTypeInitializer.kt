package io.medatarun.model.domain

data class ModelTypeInitializer(
    val id: ModelTypeId,
    val name: LocalizedText?,
    val description: LocalizedText?
)
