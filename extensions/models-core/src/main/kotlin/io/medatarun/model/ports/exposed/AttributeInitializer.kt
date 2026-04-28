package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.TextMarkdown
import io.medatarun.model.domain.TextSingleLine
import io.medatarun.model.domain.TypeRef

data class AttributeInitializer(
    val attributeKey: AttributeKey,
    val type: TypeRef,
    val optional: Boolean,
    val name: TextSingleLine?,
    val description: TextMarkdown?
)