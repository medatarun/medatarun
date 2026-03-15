package io.medatarun.type.commons.serialization

import io.medatarun.type.commons.enums.EnumWithCode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SerializationUtils {
    /**
     * Creates a serializer to encode and decode a type into a JSON String
     */
    inline fun <T> stringSerializer(
        serialName: String,
        crossinline decode: (String) -> T,
        crossinline encode: (T) -> String
    ): KSerializer<T> {
        return object : KSerializer<T> {
            override val descriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

            override fun serialize(encoder: Encoder, value: T) {
                encoder.encodeString(encode(value))
            }

            override fun deserialize(decoder: Decoder): T {
                return decode(decoder.decodeString())
            }
        }
    }

    /**
     * Creates a serializer to encode and decode an enum implementing [EnumWithCode]
     */
    inline fun <T : EnumWithCode> enumWithCodeSerializer(
        serialName: String,
        crossinline decode: (String) -> T
    ): KSerializer<T> {
        return object : KSerializer<T> {
            override val descriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

            override fun serialize(encoder: Encoder, value: T) {
                encoder.encodeString(value.code)
            }

            override fun deserialize(decoder: Decoder): T {
                return decode(decoder.decodeString())
            }
        }
    }
}