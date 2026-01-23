package io.medatarun.model.domain

sealed interface EntityAttributeRef {

    data class ById(
        val id: AttributeId
    ) : EntityAttributeRef

    data class ByKey(
        val model: ModelKey,
        val entity: EntityKey,
        val attribute: AttributeKey
    ) : EntityAttributeRef
}