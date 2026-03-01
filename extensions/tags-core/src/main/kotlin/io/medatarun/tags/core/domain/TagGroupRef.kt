package io.medatarun.tags.core.domain

import io.medatarun.type.commons.ref.Ref

sealed interface TagGroupRef : Ref<TagGroupRef> {

    data class ById(
        val id: TagGroupId
    ) : TagGroupRef {
        override fun asString(): String {
            return "id:${id.value}"
        }
    }

    data class ByKey(
        val key: TagGroupKey,
    ) : TagGroupRef {
        override fun asString(): String {
            return "key:${key.value}"
        }
    }
}