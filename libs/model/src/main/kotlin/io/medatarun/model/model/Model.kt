package io.medatarun.model.model

@JvmInline
value class ModelId(val value: String)

@JvmInline
value class ModelLocation(val value: String)


interface Model {
    val id: ModelId
    val entities: List<ModelEntity>
}

interface ModelEntity {
    val id: String
    val name: LocalizedText
}

interface LocalizedText {
    val name: String
    fun get(locale: String): String
}

interface ModelRuntime {
    fun scan(location: ModelLocation)
    fun modelList(): List<ModelId>
}


class ModelRuntimeDefault() : ModelRuntime {
    val models = mutableListOf<Model>()
    override fun scan(location: ModelLocation) {}
    override fun modelList() = models.map { it.id }
}