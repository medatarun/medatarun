package io.medatarun.ext.modeljson.internal.serializers

import io.medatarun.model.domain.LocalizedText
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

internal class LocalizedTextSerializer : KSerializer<LocalizedText> {
    override val descriptor = JsonElement.serializer().descriptor

    override fun serialize(encoder: Encoder, value: LocalizedText) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): LocalizedText {
        return when (val element = decoder.decodeSerializableValue(JsonElement.serializer())) {
            is JsonPrimitive -> LocalizedText(element.content)
            else -> throw SerializationException("Invalid format for LocalizedText")
        }
    }
}