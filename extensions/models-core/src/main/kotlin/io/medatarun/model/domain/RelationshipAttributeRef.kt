package io.medatarun.model.domain

sealed interface RelationshipAttributeRef {

    data class ById(
        val id: AttributeId
    ) : RelationshipAttributeRef

    data class ByKey(
        val model: ModelKey,
        val relationship: RelationshipKey,
        val attribute: AttributeKey
    ) : RelationshipAttributeRef
}