package io.medatarun.model.infra.db.events

import io.medatarun.model.ports.needs.ModelStorageCmd
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

/**
 * Describes a versioned event type.
 *
 * An event is identified by an [eventType] and [eventVersion].
 * This pair is unique amongst all model events.
 *
 * It is the result of a storage command [ModelStorageCmd], and when used to
 * create an event instance, this storage command's data becomes the
 * payload of the event.
 *
 * To transform a command into the resulting event payload, we need
 * a [serializer] which is a Kotlin serializer.
 *
 * This is the base item for [ModelEventRegistry]
 *
 */
data class ModelEventDescriptor<T : ModelStorageCmd>(
    /**
     * Class that holds the payload
     */
    val kClass: KClass<T>,
    /**
     * Unique event type in the registry
     */
    val eventType: String,
    /**
     * Event description type version
     */
    val eventVersion: Int,
    /**
     * Associated serializer
     */
    val serializer: KSerializer<T>
)