package io.medatarun.auth.adapters

import io.medatarun.auth.domain.ActorRole
import io.medatarun.security.AppPermission

object ActorRoleAdapters {
    fun toAppPermission(actorRole: ActorRole): AppPermission {
        return object: AppPermission {
            override val key: String
                get() = actorRole.key

        }
    }
}