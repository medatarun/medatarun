package io.medatarun.model.domain

import java.util.*

@JvmInline
value class RelationshipId(val value: UUID) {
    companion object {
        fun generate(): RelationshipId {
            return RelationshipId(UUID.randomUUID())
        }

        fun valueOfString(value: String): RelationshipId {
            return RelationshipId(UUID.fromString(value))
        }
    }
}
