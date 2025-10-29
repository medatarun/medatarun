package io.medatarun.ext.frictionlessdata

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TableSchemaFormatSerializer::class)
enum class TableSchemaFormat(val code: String) {
    DEFAULT("default"),
    EMAIL("email"),
    URI("uri"),
    BINARY("binary"),
    UUID("uuid")
    ;

    companion object {
        fun valueOfCode(code: String): TableSchemaFormat? =
            entries.firstOrNull { it.code == code }
    }
}
object TableSchemaFormatSerializer : KSerializer<TableSchemaFormat> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TableSchemaFormat", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TableSchemaFormat {
        val value = decoder.decodeString()
        return TableSchemaFormat.valueOfCode(value)
            ?: throw TableSchemaFormatUnknownException(value)
    }

    override fun serialize(encoder: Encoder, value: TableSchemaFormat) {
        encoder.encodeString(value.code)
    }
}