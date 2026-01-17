package io.medatarun.ext.frictionlessdata

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable(with = StringOrStringArraySerializer::class)
data class StringOrStringArray(val values: List<String>)

object StringOrStringArraySerializer : KSerializer<StringOrStringArray> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StringOrStringArray", PrimitiveKind.STRING)


    override fun serialize(encoder: Encoder, value: StringOrStringArray) {
        val output = encoder as JsonEncoder
        if (value.values.size == 1)
            output.encodeString(value.values.first())
        else
            output.encodeJsonElement(JsonArray(value.values.map { JsonPrimitive(it) }))
    }

    override fun deserialize(decoder: Decoder): StringOrStringArray {
        val input = decoder as JsonDecoder
        return when (val element = input.decodeJsonElement()) {
            is JsonArray ->
                StringOrStringArray(element.map { it.jsonPrimitive.content })

            is JsonPrimitive ->
                StringOrStringArray(listOf(element.content))

            else -> throw TableSchemaStringOrStringArrayException()
        }
    }


}


