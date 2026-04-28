package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.TextMarkdown
import io.medatarun.model.domain.TextSingleLine
import io.medatarun.model.domain.TypeKey

data class ModelTypeInitializer(
    val key: TypeKey,
    val name: TextSingleLine?,
    val description: TextMarkdown?
)