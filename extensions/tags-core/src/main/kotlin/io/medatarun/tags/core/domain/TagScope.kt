package io.medatarun.tags.core.domain

import io.medatarun.type.commons.id.Id
import java.util.UUID

sealed interface TagScope {
    val type: TagScopeType
    val scopeId: TagScopeId?

    val isGlobal get () = type.value == TagScopeGlobal.type.value
    val isManaged get () = isGlobal
    val isLocal get () = !isGlobal
    val isFree get () = isLocal

    object TagScopeGlobal: TagScope {
        override val type = TagScopeType("global")
        override val scopeId = null
    }
    class TagScopeLocal(
        override val type : TagScopeType,
        override val scopeId : TagScopeId
    ): TagScope {
        init {
            if (type.value== TagScopeGlobal.type.value) {
                throw IllegalArgumentException("Local scope can not use the global scope name")
            }
        }
    }
}

@JvmInline
value class TagScopeType(val value: String)

@JvmInline
value class TagScopeId(override val value: UUID): Id<TagScopeId>

