package io.medatarun.model.domain

import java.util.*

@JvmInline
value class AttributeId(val value: UUID) {
    companion object {
        fun generate(): AttributeId {
            return AttributeId(UUID.randomUUID())
        }

        fun valueOfString(value: String): AttributeId {
            return AttributeId(UUID.fromString(value))
        }
    }
}
