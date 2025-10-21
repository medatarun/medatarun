package io.medatarun.model.model

@JvmInline
value class AttributeDefId(val value: String)

interface AttributeDef {
    val id: AttributeDefId
    val name: LocalizedText?
    val type: ModelTypeId
    val optional: Boolean
    val description: LocalizedMarkdown?
}