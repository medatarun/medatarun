package io.medatarun.model.domain

sealed interface TypeRef {

    data class ById(
        val id: TypeId
    ) : TypeRef

    data class ByKey(
        val model: ModelKey,
        val type: TypeKey,
    ) : TypeRef
}