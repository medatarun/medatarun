package io.medatarun.model.model

interface ModelRuntime {
    fun scan(location: ModelLocation)
    fun modelList(): List<ModelId>
}