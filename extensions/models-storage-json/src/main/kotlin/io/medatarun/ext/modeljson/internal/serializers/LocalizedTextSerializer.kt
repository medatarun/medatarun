package io.medatarun.ext.modeljson.internal.serializers

import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.LocalizedTextMap
import io.medatarun.model.domain.LocalizedTextNotLocalized
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

internal class LocalizedTextSerializer : KSerializer<LocalizedText> {
    override val descriptor = JsonElement.serializer().descriptor

    override fun serialize(encoder: Encoder, value: LocalizedText) {
        val json = if (!value.isLocalized)
            JsonPrimitive(value.name)
        else
            JsonObject(value.all().mapValues { JsonPrimitive(it.value) })
        encoder.encodeSerializableValue(JsonElement.serializer(), json)
    }

    override fun deserialize(decoder: Decoder): LocalizedText {
        return when (val element = decoder.decodeSerializableValue(JsonElement.serializer())) {
            is JsonPrimitive -> LocalizedTextNotLocalized(element.content)
            is JsonObject -> LocalizedTextMap(element.mapValues { it.value.jsonPrimitive.content })
            else -> throw SerializationException("Invalid format for LocalizedText")
        }
    }
}