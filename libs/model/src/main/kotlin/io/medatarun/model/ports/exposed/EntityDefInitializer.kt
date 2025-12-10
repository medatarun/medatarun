package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import java.net.URL

data class EntityDefInitializer(
    val entityDefId: EntityDefId,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val identityAttribute: AttributeDefIdentityInitializer,
    val documentationHome: URL?
)