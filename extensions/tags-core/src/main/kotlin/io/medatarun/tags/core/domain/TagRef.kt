package io.medatarun.tags.core.domain

import io.medatarun.type.commons.ref.Ref

sealed interface TagRef : Ref<TagRef> {

    data class ById(
        val id: TagId
    ) : TagRef {
        override fun asString(): String {
            return "id:${id.value}"
        }
    }

    /**
     * Key reference format:
     * - free tag: "<tagKey>"
     * - managed tag: "<groupKey>/<tagKey>"
     */
    data class ByKey(
        val groupKey: TagGroupKey?,
        val key: TagKey,
    ) : TagRef {
        override fun asString(): String {
            if (groupKey == null) {
                return "key:${key.value}"
            }
            return "key:${groupKey.value}/${key.value}"
        }
    }

    companion object {
        fun tagRefKey(groupKey: TagGroupKey?, key: TagKey): ByKey {
            return ByKey(groupKey = groupKey, key = key)
        }

        fun tagRefId(value: TagId): ById {
            return ById(value)
        }
    }
}
