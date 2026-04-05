package io.medatarun.auth.domain.role

import io.medatarun.type.commons.ref.Ref

/**
 * Role reference that supports addressing by immutable id or by stable business key.
 */
sealed interface RoleRef : Ref<RoleRef> {

    data class ById(
        val id: RoleId
    ) : RoleRef {
        override fun asString(): String {
            return "id:${id.value}"
        }
    }

    data class ByKey(
        val key: RoleKey
    ) : RoleRef {
        override fun asString(): String {
            return "key:${key.value}"
        }
    }
}
