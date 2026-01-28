package io.medatarun.model.domain

sealed interface EntityAttributeRef {

    fun asString():String

    data class ById(
        val id: AttributeId
    ) : EntityAttributeRef {
        override fun asString():String = "id:" + id.value
    }

    data class ByKey(
        val key: AttributeKey
    ) : EntityAttributeRef {
        override fun asString():String = "key:" + key.value
    }
}