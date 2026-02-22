package io.medatarun.tags.core.domain

import io.medatarun.type.commons.ref.Ref

sealed interface TagScopeRef : Ref<TagScopeRef> {
    val type: TagScopeType
    val scopeId: TagScopeId?

    val isGlobal: Boolean
        get() = this is Global
    val isLocal: Boolean
        get() = this is Local
    val isManaged: Boolean
        get() = isGlobal
    val isFree: Boolean
        get() = isLocal

    fun toScope(): TagScope {
        return when (this) {
            is Global -> TagScope.TagScopeGlobal
            is Local -> TagScope.TagScopeLocal(type = type, scopeId = localScopeId)
        }
    }

    object Global : TagScopeRef {
        override val type: TagScopeType = TagScope.TagScopeGlobal.type
        override val scopeId: TagScopeId? = null

        override fun asString(): String {
            return type.value
        }
    }

    data class Local(
        override val type: TagScopeType,
        val localScopeId: TagScopeId
    ) : TagScopeRef {
        init {
            if (type.value == TagScope.TagScopeGlobal.type.value) {
                throw IllegalArgumentException("Local scope ref can not use the global scope name")
            }
        }

        override val scopeId: TagScopeId = localScopeId

        override fun asString(): String {
            return "${type.value}/${localScopeId.asString()}"
        }
    }
}
