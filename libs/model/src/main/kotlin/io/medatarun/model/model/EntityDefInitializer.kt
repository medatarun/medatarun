package io.medatarun.model.model

data class EntityDefInitializer(
    val entityDefId: EntityDefId,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val identityAttribute: AttributeDefIdentityInitializer
)