package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.AttributeDefId
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelTypeId

data class AttributeDefInitializer(
    val attributeDefId: AttributeDefId,
    val type: ModelTypeId,
    val optional: Boolean,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
)