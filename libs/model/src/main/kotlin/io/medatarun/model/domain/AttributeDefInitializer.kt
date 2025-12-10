package io.medatarun.model.domain

data class AttributeDefInitializer(
    val attributeDefId: AttributeDefId,
    val type: ModelTypeId,
    val optional: Boolean,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?
)