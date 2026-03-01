package io.medatarun.type.commons.ref

import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import io.medatarun.types.TypeJsonJsonObjectExpectedException
import io.medatarun.types.TypeJsonJsonStringExpectedException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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

class TypeJsonInvalidRefException(ref: String) : TypeJsonConverterBadFormatException("Invalid ref: $ref")
class TypeJsonInvalidRefSchemeException(ref: String) : TypeJsonConverterBadFormatException("Unsupported ref scheme: $ref")
