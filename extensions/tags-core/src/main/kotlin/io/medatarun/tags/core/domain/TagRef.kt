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
        val scopeRef: TagScopeRef,
        val groupKey: TagGroupKey?,
        val key: TagKey,
    ) : TagRef {
        init {
            if (scopeRef.isGlobal && groupKey == null) {
                throw IllegalArgumentException("Global tag ref by key requires a group key")
            }
            if (scopeRef.isLocal && groupKey != null) {
                throw IllegalArgumentException("Local tag ref by key can not provide a group key")
            }
        }

        override fun asString(): String {
            if (scopeRef.isGlobal) {
                val localGroupKey = groupKey ?: throw IllegalStateException("Global tag ref requires group key")
                return "key:${scopeRef.type.value}/${localGroupKey.value}/${key.value}"
            }
            val localScopeId = scopeRef.scopeId ?: throw IllegalStateException("Local tag ref requires scope id")
            return "key:${scopeRef.type.value}/${localScopeId.asString()}/${key.value}"
        }
    }

    companion object {
        fun tagRefKey(scopeRef: TagScopeRef, groupKey: TagGroupKey?, key: TagKey): ByKey {
            return ByKey(scopeRef = scopeRef, groupKey = groupKey, key = key)
        }

        fun tagRefId(value: TagId): ById {
            return ById(value)
        }
    }
}
