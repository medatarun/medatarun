package io.medatarun.auth.adapters

import io.medatarun.auth.domain.ActorRole
import io.medatarun.security.AppPrincipalRole

object ActorRoleAdapters {
    fun toAppPrincipalRole(actorRole: ActorRole): AppPrincipalRole {
        return object: AppPrincipalRole {
            override val key: String
                get() = actorRole.key

        }
    }
}