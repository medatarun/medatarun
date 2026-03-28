package io.medatarun.tags.core.infra.db.types

import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class TagGroupHistoryProjectionId(override val value: UUID) : Id<TagGroupHistoryProjectionId> {
    companion object {
        fun fromString(value: String): TagGroupHistoryProjectionId {
            return Id.fromString(value, ::TagGroupHistoryProjectionId)
        }
    }
}
