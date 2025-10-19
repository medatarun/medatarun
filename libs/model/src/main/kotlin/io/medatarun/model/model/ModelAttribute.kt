package io.medatarun.model.model

@JvmInline
value class ModelAttributeId(val value: String)

interface ModelAttribute {
    val id: ModelAttributeId
    val name: LocalizedText?
    val type: ModelTypeId
    val optional: Boolean
    val description: LocalizedMarkdown?
}