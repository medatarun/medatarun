package io.medatarun.model.infra.db

/**
 * Declares the stable event metadata stored in model_event for one repository
 * command. This keeps the event type/version explicit in code instead of
 * deriving them from Kotlin class names.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModelEventContract(
    val eventType: String,
    val eventVersion: Int
)
