package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class RelationshipRoleSnapshotId(override val value: UUID) : Id<RelationshipRoleSnapshotId> {
    companion object {
        fun generate(): RelationshipRoleSnapshotId {
            return RelationshipRoleSnapshotId(UuidUtils.generateV7())
        }

        fun fromString(value: String): RelationshipRoleSnapshotId {
            return RelationshipRoleSnapshotId(UuidUtils.fromString(value))
        }
    }
}
