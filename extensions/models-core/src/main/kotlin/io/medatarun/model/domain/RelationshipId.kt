package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import java.util.*

@JvmInline
value class RelationshipId(val value: UUID) {
    companion object {
        fun generate(): RelationshipId {
            return RelationshipId(UuidUtils.generateV7())
        }

        fun fromString(value: String): RelationshipId {
            return RelationshipId(UuidUtils.fromString(value))
        }
    }
}
