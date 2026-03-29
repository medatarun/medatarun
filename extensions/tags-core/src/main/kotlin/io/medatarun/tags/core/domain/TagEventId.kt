package io.medatarun.tags.core.domain

import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class TagEventId(override val value: UUID) : Id<TagEventId> {
    companion object {
        fun fromString(value: String): TagEventId {
            return Id.fromString(value, ::TagEventId)
        }
    }
}
