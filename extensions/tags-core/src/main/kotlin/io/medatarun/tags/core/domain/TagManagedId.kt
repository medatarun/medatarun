package io.medatarun.tags.core.domain

import io.medatarun.lang.uuid.UuidUtils
import java.util.UUID

@JvmInline
value class TagManagedId(val value: UUID) {
    fun asString() = value.toString()
    companion object {
        fun generate(): TagManagedId {
            return TagManagedId(UuidUtils.generateV7())
        }
        fun fromString(value: String): TagManagedId {
            return TagManagedId(UuidUtils.fromString(value))
        }
    }
}
