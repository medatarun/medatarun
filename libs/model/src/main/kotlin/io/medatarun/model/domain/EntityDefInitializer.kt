package io.medatarun.model.domain

import java.net.URL

data class EntityDefInitializer(
    val entityDefId: EntityDefId,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val identityAttribute: AttributeDefIdentityInitializer,
    val documentationHome: URL?
)