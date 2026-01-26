package io.medatarun.model.domain

sealed interface TypeRef {

    fun asString(): String

    data class ById(val id: TypeId) : TypeRef {
        override fun asString(): String {
            return "id:"+id.value
        }
    }

    data class ByKey(val key: TypeKey) : TypeRef {
        override fun asString(): String {
            return "key:"+key.value
        }
    }
}