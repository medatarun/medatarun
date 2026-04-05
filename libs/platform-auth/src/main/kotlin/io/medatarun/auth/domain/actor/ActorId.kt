package io.medatarun.auth.domain.actor

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.id.Id
import java.util.*

@JvmInline
value class ActorId(override val value: UUID): Id<ActorId> {
    companion object {
        fun generate(): ActorId {
            return ActorId(UuidUtils.generateV7())
        }
    }
}
