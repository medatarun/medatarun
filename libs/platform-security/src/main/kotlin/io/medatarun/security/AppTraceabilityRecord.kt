package io.medatarun.security

/**
 * Traceability record you can carry on business methods to keep track
 * of correlations and originated actor
 */
interface AppTraceabilityRecord {
    /**
     * Actor at the origin of the event to track.
     *
     * In actions, this will be the principal
     */
    val actorId: AppActorId

    /**
     * Describes the origin. In action, this will be `action:<actionInstanceId>`
     * The point is that origin is helpful for technical audits and descriptive.
     * Each origin should prefix its name with useful information to ensure
     * traceability.
     */
    val origin: String
}