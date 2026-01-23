package io.medatarun.model.adapters

import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedMarkdownMap
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.*

class LocalizedMarkdownTypeJsonConverter : TypeJsonConverter<LocalizedMarkdown> {
    override fun deserialize(json: JsonElement): LocalizedMarkdown {
        return when (json) {
            is JsonNull -> throw TypeJsonConverterIllegalNullException()
            is JsonArray -> throw TypeJsonConverterBadFormatException("expected a JsonObject or a JsonString")
            is JsonObject -> {
                try {
                    LocalizedMarkdownMap(json.entries.associate { it.key to it.value.jsonPrimitive.content })
                } catch(e: Exception) {
                    throw TypeJsonConverterBadFormatException("Could not parse JSON object. ${e.message}: $json")
                }
            }
            is JsonPrimitive -> {
                if (json.isString) {
                    val content = json.contentOrNull ?: throw TypeJsonConverterIllegalNullException()
                    return LocalizedMarkdownNotLocalized(content)
                } else {
                    throw TypeJsonConverterBadFormatException("expected a JsonObject or a JsonString")
                }
            }
        }
    }
}