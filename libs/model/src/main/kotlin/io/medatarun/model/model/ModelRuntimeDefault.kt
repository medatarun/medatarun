package io.medatarun.model.model

class ModelRuntimeDefault() : ModelRuntime {
    val models = mutableListOf<Model>()
    override fun scan(location: ModelLocation) {}
    override fun modelList() = models.map { it.id }
}