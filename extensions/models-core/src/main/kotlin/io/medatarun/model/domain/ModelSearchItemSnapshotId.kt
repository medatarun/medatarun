package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.UUID

@JvmInline
value class ModelSearchItemSnapshotId(override val value: UUID) : Id<ModelSearchItemSnapshotId> {
    companion object {
        fun fromString(value: String): ModelSearchItemSnapshotId {
            return ModelSearchItemSnapshotId(UuidUtils.fromString(value))
        }
    }
}
