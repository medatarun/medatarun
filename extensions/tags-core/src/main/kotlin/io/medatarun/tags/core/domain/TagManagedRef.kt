package io.medatarun.tags.core.domain

import io.medatarun.type.commons.ref.Ref

sealed interface TagManagedRef: Ref<TagManagedRef> {

    data class ById(
        val id: TagManagedId
    ) : TagManagedRef {
        override fun asString(): String {
            return "id:${id.value}"
        }
    }

    data class ByKey(
        val key: TagManagedKey,
    ) : TagManagedRef {
        override fun asString(): String {
            return "key:${key.value}"
        }
    }
}