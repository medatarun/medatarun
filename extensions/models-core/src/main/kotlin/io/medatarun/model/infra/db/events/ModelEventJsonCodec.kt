package io.medatarun.model.infra.db.events

import io.medatarun.model.infra.db.ModelRepoCmdEventPayloadDecodeException
import io.medatarun.model.infra.db.ModelRepoCmdEventPayloadEncodeException
import io.medatarun.model.infra.db.ModelEventRegistry
import io.medatarun.model.ports.needs.ModelStorageCmd
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Serializes and deserializes ModelRepoCmd as stable event payloads using the
 * explicit event metadata declared on each command class.
 */
class ModelEventJsonCodec(
    private val registry: ModelEventRegistry,
    private val json: Json
) {

    fun encode(cmd: ModelStorageCmd): ModelRepoCmdEncodedEvent {
        val entry = registry.findEntryByCmd(cmd)
        val payload = try {
            json.encodeToString(entry.serializer, cmd)
        } catch (e: SerializationException) {
            throw ModelRepoCmdEventPayloadEncodeException(entry.eventType, entry.eventVersion, e)
        }
        return ModelRepoCmdEncodedEvent(
            eventType = entry.eventType,
            eventVersion = entry.eventVersion,
            payload = payload
        )
    }

    fun decode(eventType: String, eventVersion: Int, payload: String): ModelStorageCmd {
        val entry = registry.findEntryByContract(eventType, eventVersion)
        return try {
            json.decodeFromString(entry.serializer, payload)
        } catch (e: SerializationException) {
            throw ModelRepoCmdEventPayloadDecodeException(eventType, eventVersion, e)
        }
    }


}
