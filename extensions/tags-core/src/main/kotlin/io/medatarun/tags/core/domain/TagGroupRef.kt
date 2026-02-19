package io.medatarun.tags.core.domain

sealed interface TagGroupRef {
    fun asString(): String
    data class ById(
        val id: TagGroupId
    ) : TagGroupRef {
        override fun asString(): String {
            return "id:${id.value}"
        }
    }

    data class ByKey(
        val key: TagGroupKey,
    ) : TagGroupRef {
        override fun asString(): String {
            return "key:${key.value}"
        }
    }
}