package io.medatarun.model.domain

import io.medatarun.type.commons.ref.Ref

sealed interface BusinessKeyRef: Ref<BusinessKeyRef> {
    data class ById(
        val id: BusinessKeyId
    ) : BusinessKeyRef {
        override fun asString(): String = "id:" + id.value
    }

    data class ByKey(
        val key: BusinessKeyKey
    ) : BusinessKeyRef {
        override fun asString(): String = "key:" + key.value
    }
}