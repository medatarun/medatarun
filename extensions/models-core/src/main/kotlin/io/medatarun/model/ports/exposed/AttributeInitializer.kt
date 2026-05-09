package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.AttributeKey
import io.medatarun.type.commons.text.TextMarkdown
import io.medatarun.type.commons.text.TextSingleLine
import io.medatarun.model.domain.TypeRef

data class AttributeInitializer(
    val attributeKey: AttributeKey,
    val type: TypeRef,
    val optional: Boolean,
    val name: TextSingleLine?,
    val description: TextMarkdown?
)