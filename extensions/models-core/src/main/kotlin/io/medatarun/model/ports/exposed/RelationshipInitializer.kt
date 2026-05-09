package io.medatarun.model.ports.exposed

import io.medatarun.type.commons.text.TextMarkdown
import io.medatarun.type.commons.text.TextSingleLine
import io.medatarun.model.domain.RelationshipKey

data class RelationshipInitializer(
    val key: RelationshipKey,
    val name: TextSingleLine?,
    val description: TextMarkdown?,
    val roles: List<RelationshipInitializerRole>
)