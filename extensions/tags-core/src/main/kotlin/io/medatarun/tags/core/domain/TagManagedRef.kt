package io.medatarun.tags.core.domain

sealed interface TagManagedRef {
    fun asString(): String
    data class ById(
        val id: TagManagedId
    ) : TagManagedRef {
        override fun asString(): String {
            return "id:${id.value}"
        }
    }

    data class ByKey(
        val key: TagManagedKey,
    ) : TagManagedRef {
        override fun asString(): String {
            return "key:${key.value}"
        }
    }
}