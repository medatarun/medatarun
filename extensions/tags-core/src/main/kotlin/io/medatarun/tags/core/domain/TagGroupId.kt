package io.medatarun.tags.core.domain

import io.medatarun.lang.uuid.UuidUtils
import java.util.UUID

@JvmInline
value class TagGroupId(val value: UUID) {
    fun asString() = value.toString()
    companion object {
        fun generate(): TagGroupId {
            return TagGroupId(UuidUtils.generateV7())
        }
        fun fromString(value: String): TagGroupId {
            return TagGroupId(UuidUtils.fromString(value))
        }
    }
}
