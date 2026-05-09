package io.medatarun.model.ports.exposed

import io.medatarun.type.commons.text.TextMarkdown
import io.medatarun.type.commons.text.TextSingleLine
import io.medatarun.model.domain.TypeKey

data class ModelTypeInitializer(
    val key: TypeKey,
    val name: TextSingleLine?,
    val description: TextMarkdown?
)