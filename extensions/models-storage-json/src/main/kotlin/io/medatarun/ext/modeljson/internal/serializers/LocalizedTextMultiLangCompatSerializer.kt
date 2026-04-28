package io.medatarun.ext.modeljson.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

internal class LocalizedTextMultiLangCompatSerializer : KSerializer<LocalizedTextMultiLangCompat> {
    override val descriptor = JsonElement.serializer().descriptor

    override fun serialize(encoder: Encoder, value: LocalizedTextMultiLangCompat) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): LocalizedTextMultiLangCompat {
        return when (val element = decoder.decodeSerializableValue(JsonElement.serializer())) {
            is JsonPrimitive -> LocalizedTextMultiLangCompat(element.content)
            is JsonObject -> {
                val values = element.mapValues { it.value.jsonPrimitive.content }
                    val value = values["en"] ?: values["fr"] ?: values.values.firstOrNull()
                        ?: throw SerializationException("Invalid format for LocalizedText")
                LocalizedTextMultiLangCompat(value)
            }
            else -> throw SerializationException("Invalid format for LocalizedText")
        }
    }
}