package io.medatarun.ext.modeljson.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal inline fun <reified T> valueClassSerializer(
    crossinline wrap: (String) -> T,
    crossinline unwrap: (T) -> String
): KSerializer<T> =
    object : KSerializer<T> {
        override val descriptor =
            PrimitiveSerialDescriptor(T::class.simpleName ?: "ValueClass", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: T) =
            encoder.encodeString(unwrap(value))

        override fun deserialize(decoder: Decoder): T =
            wrap(decoder.decodeString())
    }