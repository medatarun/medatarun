package io.medatarun.model.adapters

import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.LocalizedTextMap
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.*

class LocalizedTextTypeJsonConverter : TypeJsonConverter<LocalizedText> {
    override fun deserialize(json: JsonElement): LocalizedText {
        return when (json) {
            is JsonNull -> throw TypeJsonConverterIllegalNullException()
            is JsonArray -> throw TypeJsonConverterBadFormatException("expected a JsonObject or a JsonString")
            is JsonObject -> {
                try {
                    LocalizedTextMap(json.entries.associate { it.key to it.value.jsonPrimitive.content })
                } catch(e: Exception) {
                    throw TypeJsonConverterBadFormatException("Could not parse JSON object. ${e.message}: $json")
                }
            }
            is JsonPrimitive -> {
                if (json.isString) {
                    val content = json.contentOrNull ?: throw TypeJsonConverterIllegalNullException()
                    return LocalizedTextNotLocalized(content)
                } else {
                    throw TypeJsonConverterBadFormatException("expected a JsonObject or a JsonString")
                }
            }
        }
    }
}