package io.medatarun.auth.ports.needs

interface ActorRolesRegistry {
    fun isKnownRole(key: String): Boolean

}
