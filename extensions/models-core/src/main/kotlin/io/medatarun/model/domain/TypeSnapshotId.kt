package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class TypeSnapshotId(override val value: UUID) : Id<TypeSnapshotId> {
    companion object {
        fun generate(): TypeSnapshotId {
            return TypeSnapshotId(UuidUtils.generateV7())
        }

        fun fromString(value: String): TypeSnapshotId {
            return TypeSnapshotId(UuidUtils.fromString(value))
        }
    }
}
