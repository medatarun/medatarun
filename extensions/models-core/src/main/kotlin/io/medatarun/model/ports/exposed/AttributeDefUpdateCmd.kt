package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.TypeKey

sealed class AttributeDefUpdateCmd {
    data class Key(val value: AttributeKey) : AttributeDefUpdateCmd()
    data class Name(val value: LocalizedText?) : AttributeDefUpdateCmd()
    data class Description(val value: LocalizedMarkdown?) : AttributeDefUpdateCmd()
    data class Type(val value: TypeKey) : AttributeDefUpdateCmd()
    data class Optional(val value: Boolean) : AttributeDefUpdateCmd()
}