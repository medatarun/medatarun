package io.medatarun.tags.core.infra.db.tables

import io.medatarun.type.commons.id.Id
import java.util.UUID

@JvmInline
value class TagGroupHistoryProjectionId(override val value: UUID) : Id<TagGroupHistoryProjectionId> {
    companion object {
        fun fromString(value: String): TagGroupHistoryProjectionId {
            return Id.fromString(value, ::TagGroupHistoryProjectionId)
        }
    }
}
