package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.TypeJsonInvalidRefException
import io.medatarun.model.adapters.TypeJsonInvalidRefSchemeException
import io.medatarun.model.adapters.TypeJsonJsonObjectExpectedException
import io.medatarun.model.adapters.TypeJsonJsonStringExpectedException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.*
import java.net.URLDecoder

object RefTypeJsonConverters {
    fun <R> expectingString(json: JsonElement, block: (value: String) -> R): R {
        return when (json) {
            is JsonNull -> throw TypeJsonConverterIllegalNullException()
            is JsonArray -> throw TypeJsonJsonObjectExpectedException()
            is JsonObject -> throw TypeJsonJsonStringExpectedException()
            is JsonPrimitive -> {
                if (json.isString) {
                    block(json.content)
                } else {
                    throw TypeJsonJsonStringExpectedException()
                }
            }
        }
    }

    fun <R> decodeRef(json: JsonElement, whenId: (value: String) -> R, whenKey: (value: String) -> R): R {
        return expectingString(json) {
            decodeRefParts(it, whenId, whenKey)
        }

    }

    private fun <R> decodeRefParts(ref: String, whenId: (value: String) -> R, whenKey: (value: String) -> R): R {
        val (scheme, rest) = ref.split(":", limit = 2).let { splitted ->
            val valid = splitted.size == 2 && splitted[0].isNotBlank() && splitted[1].isNotBlank()
            if (!valid) throw TypeJsonInvalidRefException(ref)
            splitted[0] to splitted[1]
        }
        return when (scheme) {
            "id" -> whenId(rest)
            "key" -> whenKey(formUrlDecode(rest))
            else -> throw TypeJsonInvalidRefSchemeException(ref)
        }
    }


    private fun formUrlDecode(value: String): String =
        URLDecoder.decode(value, Charsets.UTF_8)
}