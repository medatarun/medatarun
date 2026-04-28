package io.medatarun.ext.modeljson.internal.serializers

import io.medatarun.model.domain.TextSingleLine
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

internal class TextSingleLineSerializer : KSerializer<TextSingleLine> {
    override val descriptor = PrimitiveSerialDescriptor("io.medatarun.TextSingleLineSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TextSingleLine) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): TextSingleLine {
        return TextSingleLine(decoder.decodeString())
    }
}