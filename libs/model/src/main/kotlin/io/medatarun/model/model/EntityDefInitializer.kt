package io.medatarun.model.model

import java.net.URL

data class EntityDefInitializer(
    val entityDefId: EntityDefId,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val identityAttribute: AttributeDefIdentityInitializer,
    val documentationHome: URL?
)