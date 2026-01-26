package io.medatarun.model.domain

import java.util.*

sealed interface ModelRef {

    fun asString(): String

    data class ById(
        val id: ModelId
    ) : ModelRef {
        override fun asString(): String {
            return "id:${id.value}"
        }
    }

    data class ByKey(
        val key: ModelKey,
    ) : ModelRef {
        override fun asString(): String {
            return "key:${this@ByKey.key.value}"
        }
    }

    companion object {
        fun modelRefKey(value: String): ModelRef.ByKey {
            return ModelRef.ByKey(ModelKey(value))
        }
        fun modelRefKey(value: ModelKey): ModelRef.ByKey {
            return ModelRef.ByKey(value)
        }
        fun modelRefId(value: UUID): ModelRef.ById {
            return ModelRef.ById(ModelId(value))
        }
    }
}