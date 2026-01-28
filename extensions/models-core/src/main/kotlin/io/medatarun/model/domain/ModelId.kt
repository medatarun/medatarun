package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import java.util.*

@JvmInline
value class ModelId(val value: UUID) {

    fun asString() = value.toString()

    companion object {
        fun generate(): ModelId {
            return ModelId(UuidUtils.generateV7())
        }
        fun fromString(value: String): ModelId {
            return ModelId(UuidUtils.fromString(value))
        }
    }
}
