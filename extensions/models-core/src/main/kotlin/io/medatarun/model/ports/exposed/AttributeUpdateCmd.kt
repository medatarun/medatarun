package io.medatarun.model.ports.exposed

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.TypeRef

sealed class AttributeUpdateCmd {
    data class Key(val value: AttributeKey) : AttributeUpdateCmd()
    data class Name(val value: LocalizedText?) : AttributeUpdateCmd()
    data class Description(val value: LocalizedMarkdown?) : AttributeUpdateCmd()
    data class Type(val value: TypeRef) : AttributeUpdateCmd()
    data class Optional(val value: Boolean) : AttributeUpdateCmd()
}