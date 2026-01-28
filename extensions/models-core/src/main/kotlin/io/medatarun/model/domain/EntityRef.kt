package io.medatarun.model.domain

sealed interface EntityRef {

    fun asString():String

    data class ById(
        val id: EntityId
    ) : EntityRef {
        override fun asString(): String {
            return "id:"+id.value
        }
    }

    data class ByKey(
        val key: EntityKey,
    ) : EntityRef {
        override fun asString(): String {
            return "key:"+key.value
        }
    }
}