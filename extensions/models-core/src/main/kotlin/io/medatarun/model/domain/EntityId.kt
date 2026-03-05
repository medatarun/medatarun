package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class EntityId(override val value: UUID) : Id<EntityId> {

    companion object {
        fun generate(): EntityId {
            return EntityId(UuidUtils.generateV7())
        }

        fun fromString(value: String): EntityId {
            return EntityId(UuidUtils.fromString(value))
        }
    }
}
