package io.medatarun.tags.core.domain

import io.medatarun.lang.uuid.UuidUtils
import java.util.UUID

@JvmInline
value class TagGroupId(val value: UUID) {
    companion object {
        fun generate(): TagGroupId {
            return TagGroupId(UuidUtils.generateV7())
        }
    }
}
