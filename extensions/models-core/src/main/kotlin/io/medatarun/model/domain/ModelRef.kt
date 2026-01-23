package io.medatarun.model.domain

sealed interface ModelRef {

    data class ById(
        val id: ModelId
    ) : ModelRef

    data class ByKey(
        val model: ModelKey,
    ) : ModelRef
}