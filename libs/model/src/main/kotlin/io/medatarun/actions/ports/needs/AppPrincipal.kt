package io.medatarun.actions.ports.needs

/**
 * Defines what is a principal from a business perspective
 * across application.
 *
 * Do not confuse with "actors" from the auth module,
 * this one is for business use and limited to that.
 *
 * At the end, when the application is fully built, [AppPrincipal] is derived from an actor,
 * but don't rely on that, since we need to limit system boundaries for tests and integration.
 *
 * Principal is uniquely identified with its [id].
 *
 * As a convenience we provide the [issuer] and [subject] that tells where
 * this principal comes from: which identity provider and which subject in this
 * identity provider.
 *
 * OPERATIONS MUST _NEVER_ BE BASED ON [issuer] AND [subject].
 *
 * If you need to store a reference to the principal, store its [id], which is unique and stable.
 *
 */
interface AppPrincipal {
    /**
     * Unique identifier for the principal in our system.
     */
    val id: AppPrincipalId

    /**
     * Issuer
     *
     * Indicates from where this principal comes from (external Idp or internal database, whatever)
     */
    val issuer: String

    /**
     * Subject (login or email or whatever represents this principal uniquely in issuer)
     */
    val subject: String

    /**
     * Indicates that the principal has Administrator Privileges. It is a convenient way
     * to know if admin, and redondant with [roles]. If [isAdmin] then [roles] contains some admin role.
     */
    val isAdmin: Boolean

    /**
     * Convenient way to know the name the principal, for display purposes.
     *
     * Be careful, [fullname] can change anytime. The only stable reference of the principal is its [id]
     */
    val fullname: String

    /**
     * contains roles declared in application for this user. As roles may be gathered by multiple extensions, we don't assume the list, so they are string-like objects for know.
     */
    val roles: List<AppPrincipalRole>
}
