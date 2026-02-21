package io.medatarun.type.commons.id

import io.medatarun.lang.uuid.UuidUtils
import java.util.UUID

interface Id<T: Id<T>> {
    val value: UUID
    fun asString() = value.toString()

    companion object {
        fun <T:Id<T>> fromString(value: String, constructor: (value: UUID) -> T) : T {
            return constructor(UuidUtils.fromString(value))
        }
        fun <T:Id<T>> generate(constructor: (value: UUID) -> T) : T {
            return constructor(UuidUtils.generateV7())
        }
    }
}