package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.TypeKey

data class AttributeDefInitializer(
    val attributeKey: AttributeKey,
    val type: TypeKey,
    val optional: Boolean,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
)