package io.medatarun.model.domain

sealed interface EntityAttributeRef {

    data class ById(
        val id: AttributeId
    ) : EntityAttributeRef

    data class ByKey(
        val key: AttributeKey
    ) : EntityAttributeRef
}