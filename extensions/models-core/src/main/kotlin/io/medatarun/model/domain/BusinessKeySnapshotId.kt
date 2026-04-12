package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class BusinessKeySnapshotId(override val value: UUID) : Id<BusinessKeySnapshotId> {
    companion object {
        fun generate(): BusinessKeySnapshotId {
            return BusinessKeySnapshotId(UuidUtils.generateV7())
        }

        fun fromString(value: String): BusinessKeySnapshotId {
            return BusinessKeySnapshotId(UuidUtils.fromString(value))
        }
    }
}
