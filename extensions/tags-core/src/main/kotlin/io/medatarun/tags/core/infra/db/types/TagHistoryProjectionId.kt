package io.medatarun.tags.core.infra.db.types

import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class TagHistoryProjectionId(override val value: UUID) : Id<TagHistoryProjectionId> {
    companion object {
        fun fromString(value: String): TagHistoryProjectionId {
            return Id.fromString(value, ::TagHistoryProjectionId)
        }
    }
}
