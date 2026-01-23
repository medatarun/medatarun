package io.medatarun.model.domain

sealed interface RelationshipRoleRef {

    data class ById(
        val id: RelationshipRoleId
    ) : RelationshipRoleRef

    data class ByKey(
        val model: ModelKey,
        val relationship: RelationshipKey,
        val role: RelationshipRoleKey,
    ) : RelationshipRoleRef
}