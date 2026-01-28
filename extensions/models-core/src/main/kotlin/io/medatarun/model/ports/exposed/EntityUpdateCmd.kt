package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.EntityAttributeRef
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import java.net.URL

sealed class EntityUpdateCmd {
    data class Key(val value: EntityKey) : EntityUpdateCmd()
    data class Name(val value: LocalizedText?) : EntityUpdateCmd()
    data class Description(val value: LocalizedMarkdown?) : EntityUpdateCmd()
    data class IdentifierAttribute(val value: EntityAttributeRef) : EntityUpdateCmd()
    data class DocumentationHome(val value: URL?) : EntityUpdateCmd()
}