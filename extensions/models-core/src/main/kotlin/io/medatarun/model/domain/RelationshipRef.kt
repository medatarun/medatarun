package io.medatarun.model.domain

sealed interface RelationshipRef {

    data class ById(
        val id: RelationshipId
    ) : RelationshipRef

    data class ByKey(
        val model: ModelKey,
        val relationship: RelationshipKey,
    ) : RelationshipRef
}