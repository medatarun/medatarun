package io.medatarun.model.model

data class AttributeDefIdentityInitializer(
    val attributeDefId: AttributeDefId,
    val type: ModelTypeId,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
)