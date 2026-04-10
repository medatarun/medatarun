package io.medatarun.model.domain

import io.medatarun.type.commons.ref.Ref

sealed interface EntityRef : Ref<EntityRef> {

    data class ById(
        val id: EntityId
    ) : EntityRef {
        override fun asString(): String {
            return "id:" + id.value
        }
    }

    data class ByKey(
        val key: EntityKey,
    ) : EntityRef {
        override fun asString(): String {
            return "key:" + key.value
        }
    }
    companion object {
        fun entityRefKey(value: String) =  EntityRef.ByKey(EntityKey(value))
    }
}