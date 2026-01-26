package io.medatarun.model.ports.needs

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.TypeId

sealed class ModelRepoCmdAttributeUpdate {
    data class Key(val value: AttributeKey) : ModelRepoCmdAttributeUpdate()
    data class Name(val value: LocalizedText?) : ModelRepoCmdAttributeUpdate()
    data class Description(val value: LocalizedMarkdown?) : ModelRepoCmdAttributeUpdate()
    data class Type(val value: TypeId) : ModelRepoCmdAttributeUpdate()
    data class Optional(val value: Boolean) : ModelRepoCmdAttributeUpdate()
}