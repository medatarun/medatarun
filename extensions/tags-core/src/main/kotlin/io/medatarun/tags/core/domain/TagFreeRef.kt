package io.medatarun.tags.core.domain

import java.util.UUID

sealed interface TagFreeRef {
    fun asString(): String
    data class ById(
        val id: TagFreeId
    ) : TagFreeRef {
        override fun asString(): String {
            return "id:${id.value}"
        }
    }

    data class ByKey(
        val key: TagFreeKey,
    ) : TagFreeRef {
        override fun asString(): String {
            return "key:${key.value}"
        }
    }

    companion object {
        fun tagFreeRefKey(value: String): ByKey {
            return ByKey(TagFreeKey(value))
        }
        fun tagFreeRefKey(value: TagFreeKey): ByKey {
            return ByKey(value)
        }
        fun tagFreeRefId(value: UUID): ById {
            return ById(TagFreeId(value))
        }
        fun tagFreeRefId(value: TagFreeId): ById {
            return ById(value)
        }
    }
}