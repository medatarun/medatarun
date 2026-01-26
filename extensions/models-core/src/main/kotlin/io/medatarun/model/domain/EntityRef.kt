package io.medatarun.model.domain

sealed interface EntityRef {

    data class ById(
        val id: EntityId
    ) : EntityRef

    data class ByKey(
        val key: EntityKey,
    ) : EntityRef
}