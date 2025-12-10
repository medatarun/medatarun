package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.AttributeDefId
import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText

sealed class EntityDefUpdateCmd {
    data class Id(val value: EntityDefId) : EntityDefUpdateCmd()
    data class Name(val value: LocalizedText?) : EntityDefUpdateCmd()
    data class Description(val value: LocalizedMarkdown?) : EntityDefUpdateCmd()
    data class IdentifierAttribute(val value: AttributeDefId) : EntityDefUpdateCmd()
}