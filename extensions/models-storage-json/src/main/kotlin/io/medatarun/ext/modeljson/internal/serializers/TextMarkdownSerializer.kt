package io.medatarun.ext.modeljson.internal.serializers

import io.medatarun.type.commons.text.TextMarkdown
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class TextMarkdownSerializer : KSerializer<TextMarkdown> {
    override val descriptor = PrimitiveSerialDescriptor("io.medatarun.TextMarkdownSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TextMarkdown) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): TextMarkdown {
        return TextMarkdown(decoder.decodeString())
    }
}