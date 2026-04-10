package io.medatarun.model.domain

import io.medatarun.type.commons.ref.Ref

sealed interface EntityAttributeRef : Ref<EntityAttributeRef> {


    data class ById(
        val id: AttributeId
    ) : EntityAttributeRef {
        override fun asString(): String = "id:" + id.value
    }

    data class ByKey(
        val key: AttributeKey
    ) : EntityAttributeRef {
        override fun asString(): String = "key:" + key.value
    }

    companion object {
        fun attributeRefKey(value:String) = ByKey(AttributeKey(value))
    }
}