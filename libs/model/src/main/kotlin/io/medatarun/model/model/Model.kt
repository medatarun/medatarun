package io.medatarun.model.model

@JvmInline value class ModelId(val value: String)

@JvmInline value class ModelLocation(val value: String)
@JvmInline value class ModelVersion(val value: String)


interface Model {
    val id: ModelId
    val name: LocalizedText?
    val description: LocalizedMarkdown?
    val version: ModelVersion
    val entities: List<ModelEntity>

}


