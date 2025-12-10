package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.AttributeDefId
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelTypeId

data class AttributeDefIdentityInitializer(
    val attributeDefId: AttributeDefId,
    val type: ModelTypeId,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
)