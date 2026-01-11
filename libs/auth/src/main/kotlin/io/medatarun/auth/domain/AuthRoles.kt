package io.medatarun.auth.domain

/**
 * Role from [Actor] point of vue.
 *
 * Roles are interfaces with a key, so that we may extend that later.
 *
 * The only known role from the auth system is "admin". Please use the constant and don't write "admin" everywhere
 */
data class ActorRole(val key: String) {

    /**
     * Indicates this role provides administrative permissions.
     */
    fun isAdminRole(): Boolean { return key == ADMIN.key}

    companion object {
        val ADMIN = ActorRole("admin")
    }
}