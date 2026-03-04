package io.medatarun.model.domain

import io.medatarun.type.commons.ref.Ref

sealed interface TypeRef : Ref<TypeRef> {


    data class ById(val id: TypeId) : TypeRef {
        override fun asString(): String {
            return "id:" + id.value
        }
    }

    data class ByKey(val key: TypeKey) : TypeRef {
        override fun asString(): String {
            return "key:" + key.value
        }
    }
}