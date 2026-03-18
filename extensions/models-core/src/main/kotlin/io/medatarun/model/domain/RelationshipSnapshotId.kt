package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class RelationshipSnapshotId(override val value: UUID) : Id<RelationshipSnapshotId> {
    companion object {
        fun generate(): RelationshipSnapshotId {
            return RelationshipSnapshotId(UuidUtils.generateV7())
        }

        fun fromString(value: String): RelationshipSnapshotId {
            return RelationshipSnapshotId(UuidUtils.fromString(value))
        }
    }
}
