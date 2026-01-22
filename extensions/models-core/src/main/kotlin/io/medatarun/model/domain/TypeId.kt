package io.medatarun.model.domain

import java.util.*

@JvmInline
value class TypeId(val value: UUID) {
    companion object {
        fun generate(): TypeId {
            return TypeId(UUID.randomUUID())
        }

        fun valueOfString(value: String): TypeId {
            return TypeId(UUID.fromString(value))
        }
    }
}
