package io.medatarun.model.domain

import io.medatarun.type.commons.ref.Ref

sealed interface RelationshipAttributeRef : Ref<RelationshipAttributeRef> {


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