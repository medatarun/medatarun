package io.medatarun.security

/**
 * Used to resolve an actor, the sense of "somebody or something did an action"
 */
interface AppActorResolver {
    /**
     * Use this to resolve display names or coordinates of registered actors in the system by modules.
     */
    fun resolve(appActorId: AppActorId): AppActor?
}