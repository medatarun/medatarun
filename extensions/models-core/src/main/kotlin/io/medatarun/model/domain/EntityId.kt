package io.medatarun.model.domain

import java.util.*

@JvmInline
value class EntityId(val value: UUID){
    companion object {
        fun generate(): EntityId {
            return EntityId(UUID.randomUUID())
        }

        fun valueOfString(value: String): EntityId {
            return EntityId(UUID.fromString(value))
        }
    }
}
