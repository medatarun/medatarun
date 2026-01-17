package io.medatarun.ext.frictionlessdata

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable(with = StringOrNumberSerializer::class)
sealed class StringOrNumber {
    data class Str(val value: String) : StringOrNumber()
    data class Num(val value: Number) : StringOrNumber()
}


object StringOrNumberSerializer : KSerializer<StringOrNumber> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringOrNumber", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): StringOrNumber {
        val input = decoder as? JsonDecoder ?: error("Ce sérialiseur ne marche que pour du JSON")
        val primitive = input.decodeJsonElement().jsonPrimitive

        return when {
            primitive.isString ->
                StringOrNumber.Str(primitive.content)

            primitive.longOrNull != null ->
                StringOrNumber.Num(primitive.long)

            primitive.doubleOrNull != null ->
                StringOrNumber.Num(primitive.double)

            else -> error("Valeur non supportée : $primitive")
        }
    }

    override fun serialize(encoder: Encoder, value: StringOrNumber) {
        val output = encoder as? JsonEncoder ?: error("Ce sérialiseur ne marche que pour du JSON")

        when (value) {
            is StringOrNumber.Str -> output.encodeString(value.value)
            is StringOrNumber.Num -> {
                when (val n = value.value) {
                    is Int -> output.encodeInt(n)
                    is Long -> output.encodeLong(n)
                    is Double -> output.encodeDouble(n)
                    else -> output.encodeDouble(n.toDouble())
                }
            }
        }
    }
}
