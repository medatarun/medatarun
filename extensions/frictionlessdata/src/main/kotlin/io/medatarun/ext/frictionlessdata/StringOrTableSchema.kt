package io.medatarun.ext.frictionlessdata

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable(with = StringOrTableSchemaSerializer::class)
sealed class StringOrTableSchema {
    data class Str(val value: String) : StringOrTableSchema()
    data class Schema(val value: TableSchema) : StringOrTableSchema()
}


object StringOrTableSchemaSerializer : KSerializer<StringOrTableSchema> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringOrTableSchema", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): StringOrTableSchema {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonPrimitive) {
            if (element.isString) {
                return StringOrTableSchema.Str(element.content)
            } else  throw StringOrTableSchemaDecodeException()
        } else if (element is JsonObject) {
            val tableSchema = decoder.json.decodeFromJsonElement(TableSchema.serializer(), element)
            return StringOrTableSchema.Schema(tableSchema)
        } else throw StringOrTableSchemaDecodeException()
    }

    override fun serialize(
        encoder: Encoder,
        value: StringOrTableSchema
    ) {
        throw NotImplementedError()
    }

}

