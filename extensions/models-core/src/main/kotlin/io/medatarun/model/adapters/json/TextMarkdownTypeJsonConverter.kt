package io.medatarun.model.adapters.json

import io.medatarun.lang.strings.trimToNull
import io.medatarun.model.domain.TextMarkdown
import io.medatarun.types.TypeJsonConverter
import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.*

class TextMarkdownTypeJsonConverter : TypeJsonConverter<TextMarkdown> {
    override fun deserialize(json: JsonElement): TextMarkdown {
        return when (json) {
            is JsonNull -> throw TypeJsonConverterIllegalNullException()
            is JsonArray -> throw TypeJsonConverterBadFormatException("expected a JsonString")
            is JsonObject -> throw TypeJsonConverterBadFormatException("expected a JsonString")
            is JsonPrimitive -> {
                if (json.isString) {
                    val content = json.contentOrNull ?: throw TypeJsonConverterIllegalNullException()
                    val contentTrimmed = content.trimToNull()
                    if (contentTrimmed == null) throw TypeJsonConverterBadFormatException("Can not be empty")
                    else TextMarkdown(content)
                } else {
                    throw TypeJsonConverterBadFormatException("expected a JsonObject or a JsonString")
                }
            }
        }
    }
}