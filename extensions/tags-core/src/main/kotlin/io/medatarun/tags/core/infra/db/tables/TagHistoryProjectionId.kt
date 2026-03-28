package io.medatarun.tags.core.infra.db.tables

import io.medatarun.type.commons.id.Id
import java.util.UUID

@JvmInline
value class TagHistoryProjectionId(override val value: UUID) : Id<TagHistoryProjectionId> {
    companion object {
        fun fromString(value: String): TagHistoryProjectionId {
            return Id.fromString(value, ::TagHistoryProjectionId)
        }
    }
}
