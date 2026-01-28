package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import java.util.*

@JvmInline
value class TypeId(val value: UUID) {
    companion object {
        fun generate(): TypeId {
            return TypeId(UuidUtils.generateV7())
        }

        fun fromString(value: String): TypeId {
            return TypeId(UuidUtils.fromString(value))
        }
    }
}
