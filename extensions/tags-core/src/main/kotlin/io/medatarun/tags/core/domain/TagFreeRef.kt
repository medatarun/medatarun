package io.medatarun.tags.core.domain

import io.medatarun.type.commons.ref.Ref

sealed interface TagFreeRef: Ref<TagFreeRef> {

    data class ById(
        val id: TagFreeId
    ) : TagFreeRef {
        override fun asString(): String {
            return "id:${id.value}"
        }
    }

    data class ByKey(
        val key: TagFreeKey,
    ) : TagFreeRef {
        override fun asString(): String {
            return "key:${key.value}"
        }
    }

    companion object {
        fun tagFreeRefKey(value: TagFreeKey): ByKey {
            return ByKey(value)
        }

        fun tagFreeRefId(value: TagFreeId): ById {
            return ById(value)
        }
    }
}