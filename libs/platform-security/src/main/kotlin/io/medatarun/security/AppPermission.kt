package io.medatarun.security

/**
 * Defines a permission across application.
 *
 * No not confuse this with actor roles and permissions from the auth module.
 *
 * They are similar, but this one is for business uses.
 */
interface AppPermission {
    val key: String
    val name: String? get() = null
    val description: String? get() = null

    /**
     * Some permissions have no sense if another permission is not present.
     * This is the list of dependencies. For example, if you can write something
     * and are not able to read it, it is a problem.
     */
    val implies: List<AppPermission> get() = emptyList()
}