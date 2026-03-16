package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class ModelSnapshotId(override val value: UUID) : Id<ModelSnapshotId> {
    companion object {
        fun generate(): ModelSnapshotId {
            return ModelSnapshotId(UuidUtils.generateV7())
        }

        fun fromString(value: String): ModelSnapshotId {
            return ModelSnapshotId(UuidUtils.fromString(value))
        }
    }
}
