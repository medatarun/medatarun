package io.medatarun.model.domain

data class AttributeDefIdentityInitializer(
    val attributeDefId: AttributeDefId,
    val type: ModelTypeId,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
)