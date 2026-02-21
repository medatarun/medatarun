package io.medatarun.model.domain

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class RelationshipId(override val value: UUID): Id<RelationshipId> {

    companion object {
        fun generate(): RelationshipId {
            return RelationshipId(UuidUtils.generateV7())
        }

        fun fromString(value: String): RelationshipId {
            return RelationshipId(UuidUtils.fromString(value))
        }
    }
}
