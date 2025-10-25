package io.medatarun.model.model

sealed class EntityDefUpdateCmd {
    data class Id(val value: EntityDefId) : EntityDefUpdateCmd()
    data class Name(val value: LocalizedText?) : EntityDefUpdateCmd()
    data class Description(val value: LocalizedMarkdown?) : EntityDefUpdateCmd()
    data class IdentifierAttribute(val value: AttributeDefId) : EntityDefUpdateCmd()
}