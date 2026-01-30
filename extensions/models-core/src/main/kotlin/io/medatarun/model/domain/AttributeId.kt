package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import java.util.*

@JvmInline
value class AttributeId(val value: UUID) {
    fun asString() = value.toString()
    companion object {
        fun generate(): AttributeId {
            return AttributeId(UuidUtils.generateV7())
        }

        fun fromString(value: String): AttributeId {
            return AttributeId(UuidUtils.fromString(value))
        }
    }
}
