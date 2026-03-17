package io.medatarun.security

import io.medatarun.type.commons.id.Id
import java.util.UUID

/**
 * Defines an actor id in the scope of application.
 */
@JvmInline
value class AppActorId(override val value: UUID): Id<AppActorId> {
    companion object {
        fun generate(): AppActorId {
            return Id.generate(::AppActorId)
        }
    }
}
