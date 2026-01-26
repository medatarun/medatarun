package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.EntityAttributeRef
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import java.net.URL

sealed class EntityDefUpdateCmd {
    data class Key(val value: EntityKey) : EntityDefUpdateCmd()
    data class Name(val value: LocalizedText?) : EntityDefUpdateCmd()
    data class Description(val value: LocalizedMarkdown?) : EntityDefUpdateCmd()
    data class IdentifierAttribute(val value: EntityAttributeRef) : EntityDefUpdateCmd()
    data class DocumentationHome(val value: URL?) : EntityDefUpdateCmd()
}