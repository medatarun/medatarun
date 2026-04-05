package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.ActorPermission

interface PermissionsRegistry {
    fun isKnownPermission(key: String): Boolean
    fun isKnownPermission(p: ActorPermission): Boolean = isKnownPermission(p.key)

}
