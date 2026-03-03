package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class AttributeId(override val value: UUID): Id<AttributeId> {

    companion object {
        fun generate(): AttributeId {
            return AttributeId(UuidUtils.generateV7())
        }

        fun fromString(value: String): AttributeId {
            return AttributeId(UuidUtils.fromString(value))
        }
    }
}
