package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class EntityPKSnapshotId(override val value: UUID) : Id<EntityPKSnapshotId> {
    companion object {
        fun generate(): EntityPKSnapshotId {
            return EntityPKSnapshotId(UuidUtils.generateV7())
        }

        fun fromString(value: String): EntityPKSnapshotId {
            return EntityPKSnapshotId(UuidUtils.fromString(value))
        }
    }
}
