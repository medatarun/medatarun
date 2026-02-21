package io.medatarun.tags.core.domain

import io.medatarun.lang.uuid.UuidUtils
import java.util.UUID

@JvmInline
value class TagFreeId(val value: UUID) {
    fun asString() = value.toString()
    companion object {
        fun generate(): TagFreeId {
            return TagFreeId(UuidUtils.generateV7())
        }

        fun fromString(value: String): TagFreeId {
            return TagFreeId(UuidUtils.fromString(value))
        }
    }
}
