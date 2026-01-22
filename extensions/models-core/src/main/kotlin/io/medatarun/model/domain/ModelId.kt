package io.medatarun.model.domain

import java.util.*

@JvmInline
value class ModelId(val value: UUID) {
    companion object {
        fun generate(): ModelId {
            return ModelId(UUID.randomUUID())
        }
        fun valueOfString(value: String): ModelId {
            return ModelId(UUID.fromString(value))
        }
    }
}
