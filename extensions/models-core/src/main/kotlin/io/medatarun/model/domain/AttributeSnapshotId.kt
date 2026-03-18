package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class AttributeSnapshotId(override val value: UUID) : Id<AttributeSnapshotId> {
    companion object {
        fun generate(): AttributeSnapshotId {
            return AttributeSnapshotId(UuidUtils.generateV7())
        }

        fun fromString(value: String): AttributeSnapshotId {
            return AttributeSnapshotId(UuidUtils.fromString(value))
        }
    }
}
