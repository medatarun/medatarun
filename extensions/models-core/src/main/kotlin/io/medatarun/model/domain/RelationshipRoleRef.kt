package io.medatarun.model.domain

sealed interface RelationshipRoleRef {
    fun asString(): String
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