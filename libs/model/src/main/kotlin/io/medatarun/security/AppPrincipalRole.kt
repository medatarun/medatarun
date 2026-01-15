package io.medatarun.security

/**
 * Defines a role across application.
 *
 * No not confuse this with actor roles from the auth module.
 *
 * They are similar, but this one is for business uses.
 */
interface AppPrincipalRole {
    val key: String
}