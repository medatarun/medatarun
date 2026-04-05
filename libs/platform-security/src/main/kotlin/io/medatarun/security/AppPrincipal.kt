package io.medatarun.security

/**
 * Defines what is **the** principal from a business perspective
 * across application. The principal is the signed-in actor that holds the permissions.
 *
 * Do not confuse with [AppActor] which represents any actor that can act on the
 * application.
 *
 * Do not confuse with "actors" from the auth module because nobody
 * should know those actors except the auth module itself.
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
    val id: AppActorId

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
     * to know if admin, and redondant with [permissions].
     *
     * If [isAdmin] then [permissions] contains some admin permission.
     */
    val isAdmin: Boolean

    /**
     * Convenient way to know the name the principal, for display purposes.
     *
     * Be careful, [fullname] can change anytime. The only stable reference of the principal is its [id]
     */
    val fullname: String

    /**
     * Contains permissions declared in the application for this user.
     * Permissions usually come from actor roles in auth module and may
     * be gathered by multiple extensions;
     *
     * Don't assume we know the list, so they are string-like objects for know.
     */
    val permissions: Set<AppPermission>
}