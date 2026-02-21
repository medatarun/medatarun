package io.medatarun.model.domain

import io.medatarun.type.commons.ref.Ref

sealed interface RelationshipRoleRef: Ref<RelationshipRoleRef> {

    data class ById(
        val id: RelationshipRoleId
    ) : RelationshipRoleRef {
        override fun asString() = "id:"+id.value.toString()
    }

    data class ByKey(
        val key: RelationshipRoleKey,
    ) : RelationshipRoleRef {
        override fun asString() = "key:"+key.value
    }
}