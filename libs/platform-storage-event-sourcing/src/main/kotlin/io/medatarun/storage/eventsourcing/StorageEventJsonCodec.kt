package io.medatarun.storage.eventsourcing

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Serializes and deserializes [StorageCmd] as stable event payloads using the
 * explicit event metadata declared on each command class.
 */
class StorageEventJsonCodec<T: StorageCmd>(
    private val registry: StorageEventRegistry<T>,
    private val json: Json
) {

    /**
     * Encode a model event payload expressed as a [StorageCmd]
     * into a [io.medatarun.storage.eventsourcing.StorageEventEncoded], suitable for a database.
     */
    fun encode(cmd: T): StorageEventEncoded {
        val entry = registry.findEntryByCmd(cmd)
        val payload = try {
            json.encodeToString(entry.serializer, cmd)
        } catch (e: SerializationException) {
            throw StorageEventPayloadEncodeException(entry.eventType, entry.eventVersion, e)
        }
        return StorageEventEncoded(
            eventType = entry.eventType,
            eventVersion = entry.eventVersion,
            payload = payload
        )
    }

    /**
     * Decodes an event into a [StorageCmd]
     */
    fun decode(evt: StorageEventEncoded): T {
        val entry = registry.findEntryByContract(evt.eventType, evt.eventVersion)
        return try {
            json.decodeFromString(entry.serializer, evt.payload)
        } catch (e: SerializationException) {
            throw StorageEventPayloadDecodeException(evt.eventType, evt.eventVersion, e)
        }
    }


}
