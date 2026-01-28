package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import java.util.*

@JvmInline
value class EntityId(val value: UUID){
    companion object {
        fun generate(): EntityId {
            return EntityId(UuidUtils.generateV7())
        }

        fun fromString(value: String): EntityId {
            return EntityId(UuidUtils.fromString(value))
        }
    }
}
