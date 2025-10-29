package io.medatarun.ext.frictionlessdata

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = StringOrTableSchemaSerializer::class)
sealed class StringOrTableSchema {
    data class Str(val value: String): StringOrTableSchema()
    data class Schema(val value: TableSchema): StringOrTableSchema()
}


object StringOrTableSchemaSerializer : KSerializer<StringOrTableSchema> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringOrTableSchema", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): StringOrTableSchema {
        val primitive = (decoder as JsonDecoder).decodeJsonElement().jsonPrimitive
        if (primitive.isString) {
            return StringOrTableSchema.Str(primitive.content)
        } else {
            val tableSchema = decoder.decodeSerializableValue(TableSchema.serializer())
            return StringOrTableSchema.Schema(tableSchema)
        }

    }

    override fun serialize(
        encoder: Encoder,
        value: StringOrTableSchema
    ) {
        throw NotImplementedError()
    }

}

