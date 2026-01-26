package io.medatarun.model.domain

sealed interface RelationshipRef {
    fun asString(): String
    data class ById(
        val id: RelationshipId
    ) : RelationshipRef {
        override fun asString(): String = "id:"+id.value
    }

    data class ByKey(
        val key: RelationshipKey,
    ) : RelationshipRef {
        override fun asString(): String = "key:"+key.value
    }
}