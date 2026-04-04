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
}