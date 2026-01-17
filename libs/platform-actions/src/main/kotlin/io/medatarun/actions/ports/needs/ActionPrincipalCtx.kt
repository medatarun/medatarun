package io.medatarun.actions.ports.needs

import io.medatarun.security.AppPrincipal

/**
 * Actions can ask questions about the principal
 * from this context.
 *
 * This is a wrapper around the [io.medatarun.security.AppPrincipal], when there is one,
 * with utility methods.
 *
 * Also works when no principal is connected.
 *
 * This is the contract between actions and security
 */
interface ActionPrincipalCtx {
    /**
     * Ensure the principal is signed in and is an admin
     */
    fun ensureIsAdmin()
    /**
     * Ensure the principal is signed in
     */
    fun ensureSignedIn(): AppPrincipal



    /**
     * Returns the principal if there is one signed in
     */
    val principal: AppPrincipal?
}