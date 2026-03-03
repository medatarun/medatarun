package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class TypeId(override val value: UUID): Id<TypeId> {
    companion object {
        fun generate(): TypeId {
            return TypeId(UuidUtils.generateV7())
        }

        fun fromString(value: String): TypeId {
            return TypeId(UuidUtils.fromString(value))
        }
    }
}
