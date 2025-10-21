package io.medatarun.model.model

sealed class AttributeDefUpdateCmd {
    data class Id(val value: AttributeDefId) : AttributeDefUpdateCmd()
    data class Name(val value: LocalizedText?) : AttributeDefUpdateCmd()
    data class Description(val value: LocalizedMarkdown?) : AttributeDefUpdateCmd()
    data class Type(val value: ModelTypeId) : AttributeDefUpdateCmd()
    data class Optional(val value: Boolean) : AttributeDefUpdateCmd()
}
