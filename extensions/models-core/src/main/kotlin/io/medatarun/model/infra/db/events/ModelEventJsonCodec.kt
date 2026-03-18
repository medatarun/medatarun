package io.medatarun.model.infra.db.events

import io.medatarun.model.infra.db.ModelRepoCmdEventPayloadDecodeException
import io.medatarun.model.infra.db.ModelRepoCmdEventPayloadEncodeException
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

    /**
     * Encode a model event payload expressed as a [ModelStorageCmd]
     * into a [ModelEventEncoded], suitable for a database.
     */
    fun encode(cmd: ModelStorageCmd): ModelEventEncoded {
        val entry = registry.findEntryByCmd(cmd)
        val payload = try {
            json.encodeToString(entry.serializer, cmd)
        } catch (e: SerializationException) {
            throw ModelRepoCmdEventPayloadEncodeException(entry.eventType, entry.eventVersion, e)
        }
        return ModelEventEncoded(
            eventType = entry.eventType,
            eventVersion = entry.eventVersion,
            payload = payload
        )
    }

    /**
     * Decodes an event into a [ModelStorageCmd]
     */
    fun decode(evt: ModelEventEncoded): ModelStorageCmd {
        val entry = registry.findEntryByContract(evt.eventType, evt.eventVersion)
        return try {
            json.decodeFromString(entry.serializer, evt.payload)
        } catch (e: SerializationException) {
            throw ModelRepoCmdEventPayloadDecodeException(evt.eventType, evt.eventVersion, e)
        }
    }


}
