package io.medatarun.model.model

@JvmInline
value class ModelEntityId(val value: String)

interface ModelEntity {
    fun countAttributes(): Int
    fun getAttribute(id: ModelAttributeId): ModelAttribute

    val id: ModelEntityId
    val name: LocalizedText?
    val description: LocalizedMarkdown?
    val attributes: List<ModelAttribute>
}