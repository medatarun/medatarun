package io.medatarun.model.domain

sealed interface RelationshipAttributeRef {

    fun asString():String

    data class ById(
        val id: AttributeId
    ) : RelationshipAttributeRef {
        override fun asString(): String = "id:" + id.value
    }

    data class ByKey(
        val key: AttributeKey
    ) : RelationshipAttributeRef {
        override fun asString(): String = "key:" + key.value
    }
}