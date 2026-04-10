package io.medatarun.ext.modeljson.internal.serializers

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedMarkdownMap
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

internal class LocalizedMarkdownSerializer : KSerializer<LocalizedMarkdown> {
    override val descriptor = JsonElement.serializer().descriptor

    override fun serialize(encoder: Encoder, value: LocalizedMarkdown) {
        val json = if (!value.isLocalized)
            JsonPrimitive(value.name)
        else
            JsonObject(value.all().mapValues { JsonPrimitive(it.value) })
        encoder.encodeSerializableValue(JsonElement.serializer(), json)
    }

    override fun deserialize(decoder: Decoder): LocalizedMarkdown {
        return when (val element = decoder.decodeSerializableValue(JsonElement.serializer())) {
            is JsonPrimitive -> LocalizedMarkdownNotLocalized(element.content)
            is JsonObject -> LocalizedMarkdownMap(element.mapValues { it.value.jsonPrimitive.content })
            else -> throw SerializationException("Invalid format for LocalizedText")
        }
    }
}