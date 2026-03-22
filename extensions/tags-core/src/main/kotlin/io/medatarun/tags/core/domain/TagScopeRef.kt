package io.medatarun.tags.core.domain

import io.medatarun.type.commons.id.Id
import io.medatarun.type.commons.ref.Ref
import java.util.*

sealed interface TagScopeRef : Ref<TagScopeRef> {
    val type: TagScopeType
    val scopeId: TagScopeId?

    val isGlobal: Boolean
        get() = this is Global
    val isLocal: Boolean
        get() = this is Local

    object Global : TagScopeRef {
        override val type: TagScopeType = TagScopeType("global")
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
            if (type.value == Global.type.value) {
                throw TagScopeRefLocalUsesGlobalTypeException()
            }
        }

        override val scopeId: TagScopeId = localScopeId

        override fun asString(): String {
            return "${type.value}/${localScopeId.asString()}"
        }
    }
}

@JvmInline
value class TagScopeType(val value: String)

@JvmInline
value class TagScopeId(override val value: UUID): Id<TagScopeId>
