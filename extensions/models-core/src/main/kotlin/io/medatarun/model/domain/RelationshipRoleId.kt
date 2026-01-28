package io.medatarun.model.domain

import java.util.*

@JvmInline
value class RelationshipRoleId(val value: UUID) {
    companion object {
        fun generate(): RelationshipRoleId = RelationshipRoleId(UUID.randomUUID())
        fun valueOfString(value: String): RelationshipRoleId {
                return RelationshipRoleId(UUID.fromString(value))
        }
    }
}