package io.medatarun.tags.core.domain

sealed interface FreeTagRef {
    fun asString(): String
    data class ById(
        val id: TagFreeId
    ) : FreeTagRef {
        override fun asString(): String {
            return "id:${id.value}"
        }
    }

    data class ByKey(
        val key: TagFreeKey,
    ) : FreeTagRef {
        override fun asString(): String {
            return "key:${key.value}"
        }
    }
}