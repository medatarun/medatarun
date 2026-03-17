package io.medatarun.model.domain

import io.medatarun.type.commons.ref.Ref
import java.util.*

sealed interface ModelRef : Ref<ModelRef> {

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
        fun modelRefKey(value: String): ByKey {
            return ByKey(ModelKey(value))
        }

        fun modelRefKey(value: ModelKey): ByKey {
            return ByKey(value)
        }

        fun modelRefId(value: ModelId): ById {
            return ById(value)
        }
    }
}
