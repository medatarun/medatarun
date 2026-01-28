package io.medatarun.auth.domain.actor

import io.medatarun.lang.uuid.UuidUtils
import java.util.*

@JvmInline
value class ActorId(val value: UUID) {
    companion object {
        fun generate(): ActorId {
            return ActorId(UuidUtils.generateV7())
        }
        fun fromString(value: String): ActorId {
            return ActorId(UuidUtils.fromString(value))
        }
    }
}
