package io.medatarun.model.domain

import io.medatarun.type.commons.ref.Ref
import java.util.UUID

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
        fun entityAttributeRefKey(value:String) = ByKey(AttributeKey(value))
        fun entityAttributeRefKey(value: AttributeKey) = ByKey(value)
        fun entityAttributeRefId(value: UUID) = ById(AttributeId(value))
        fun entityAttributeRefId(value: AttributeId) = ById(value)
    }
}