package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class RelationshipRoleId(override val value: UUID) : Id<RelationshipRoleId> {
    companion object {
        fun generate(): RelationshipRoleId {
            return RelationshipRoleId(UuidUtils.generateV7())
        }

        fun fromString(value: String): RelationshipRoleId {
            return RelationshipRoleId(UuidUtils.fromString(value))
        }
    }
}