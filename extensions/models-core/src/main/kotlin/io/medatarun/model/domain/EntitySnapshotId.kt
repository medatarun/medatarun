package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class EntitySnapshotId(override val value: UUID) : Id<EntitySnapshotId> {
    companion object {
        fun generate(): EntitySnapshotId {
            return EntitySnapshotId(UuidUtils.generateV7())
        }

        fun fromString(value: String): EntitySnapshotId {
            return EntitySnapshotId(UuidUtils.fromString(value))
        }
    }
}
