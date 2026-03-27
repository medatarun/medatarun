package io.medatarun.storage.eventsourcing

/**
 * Declares the stable event metadata stored in event tables (model_event, tag_event)
 * for one storage command. This keeps the event type/version explicit in code instead of
 * deriving them from Kotlin class names.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class StorageEventContract(
    val eventType: String,
    val eventVersion: Int
)