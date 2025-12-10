package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelTypeId

data class ModelTypeInitializer(
    val id: ModelTypeId,
    val name: LocalizedText?,
    val description: LocalizedText?
)