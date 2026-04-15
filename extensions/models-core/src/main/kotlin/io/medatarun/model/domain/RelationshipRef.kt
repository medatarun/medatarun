package io.medatarun.model.domain

import io.medatarun.type.commons.ref.Ref

sealed interface RelationshipRef : Ref<RelationshipRef> {

    data class ById(
        val id: RelationshipId
    ) : RelationshipRef {
        override fun asString(): String = "id:" + id.value
    }

    data class ByKey(
        val key: RelationshipKey,
    ) : RelationshipRef {
        override fun asString(): String = "key:" + key.value
    }

    companion object {
        fun relationshipRefKey(value: String) = ByKey(RelationshipKey(value))
    }
}