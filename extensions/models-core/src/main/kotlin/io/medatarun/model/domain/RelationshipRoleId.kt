package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import java.util.*

@JvmInline
value class RelationshipRoleId(val value: UUID) {
    companion object {
        fun generate(): RelationshipRoleId {
            return RelationshipRoleId(UuidUtils.generateV7())
        }

        fun fromString(value: String): RelationshipRoleId {
            return RelationshipRoleId(UuidUtils.fromString(value))
        }
    }
}