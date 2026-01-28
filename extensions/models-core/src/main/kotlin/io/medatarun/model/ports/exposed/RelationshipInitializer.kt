package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.RelationshipKey

data class RelationshipInitializer(
    val key: RelationshipKey,
    val name: LocalizedText?,
    val description: LocalizedMarkdown?,
    val roles: List<RelationshipInitializerRole>
)