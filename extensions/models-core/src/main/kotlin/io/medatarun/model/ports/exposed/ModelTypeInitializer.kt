package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.TypeKey

data class ModelTypeInitializer(
    val id: TypeKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
)