package io.medatarun.auth.adapters

import io.medatarun.auth.domain.ActorPermission
import io.medatarun.security.AppPermission

object ActorRoleAdapters {
    fun toAppPermission(actorPermission: ActorPermission): AppPermission {
        return object: AppPermission {
            override val key: String
                get() = actorPermission.key

        }
    }
}