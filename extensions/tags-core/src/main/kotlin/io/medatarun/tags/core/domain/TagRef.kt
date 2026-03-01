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
     * - managed/global tag: "global/<groupKey>/<tagKey>"
     * - local/free tag: "<scopeType>/<scopeId>/<tagKey>"
     */
    data class ByKey(
        val scopeRef: TagScopeRef,
        val groupKey: TagGroupKey?,
        val key: TagKey,
    ) : TagRef {
        init {
            if (scopeRef.isGlobal && groupKey == null) {
                throw TagRefGlobalByKeyMissingGroupKeyException()
            }
            if (scopeRef.isLocal && groupKey != null) {
                throw TagRefLocalByKeyUnexpectedGroupKeyException()
            }
        }

        override fun asString(): String {
            if (scopeRef is TagScopeRef.Global) {
                val localGroupKey = groupKey ?: throw TagRefGlobalByKeySerializationMissingGroupKeyException()
                return "key:${scopeRef.type.value}/${localGroupKey.value}/${key.value}"
            }
            val localScopeRef = scopeRef as TagScopeRef.Local
            val localScopeId = localScopeRef.scopeId
            return "key:${localScopeRef.type.value}/${localScopeId.asString()}/${key.value}"
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
