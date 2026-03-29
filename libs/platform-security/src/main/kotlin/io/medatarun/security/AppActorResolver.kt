package io.medatarun.security

/**
 * Used to resolve an actor, the sense of "somebody or something did an action"
 */
interface AppActorResolver {
    /**
     * Use this to resolve display names or coordinates of registered actors in the system by modules.
     */
    fun resolve(appActorId: AppActorId): AppActor?

    /**
     * Returns the system maintenance actor. It is a special actor you can
     * use to identify operations done by the system itself.
     *
     * System actor has a stable id and a specific issuer across all
     * installations of the application.
     *
     * Typically used for data migrations or audit logs to say "the system did
     * it itself for maintenance".
     */
    fun resolveSystemMaintenanceActor(): AppActorSystemMaintenance = AppActorSystemMaintenance
}