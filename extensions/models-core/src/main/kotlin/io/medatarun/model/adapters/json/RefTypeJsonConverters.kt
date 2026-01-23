package io.medatarun.model.adapters.json

import io.medatarun.model.adapters.*
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

    class KeyParts(val parts:Map<String,String>): Map<String,String> by parts {
        fun required(name: String): String {
            return this[name] ?: throw TypeJsonInvalidRefMissingKeyException(name)
        }
    }

    fun <R> decodeRef(json: JsonElement, whenId: (value: String) -> R, whenKey: (params: KeyParts) -> R):R {
        return expectingString(json) {
            decodeRefParts(it, whenId, whenKey)
        }

    }

    private fun <R> decodeRefParts(ref: String, whenId: (value: String) -> R, whenKey: (params: KeyParts) -> R): R {
        val (scheme, rest) = ref.split(":", limit = 2).let { splitted ->
            val valid = splitted.size == 2 && splitted[0].isNotBlank() && splitted[1].isNotBlank()
            if (!valid) throw TypeJsonInvalidRefException(ref)
            splitted[0] to splitted[1]
        }
        return when (scheme) {
            "id" -> whenId(rest)
            "key" -> whenKey(parseQuery(rest))
            else -> throw TypeJsonInvalidRefSchemeException(ref)
        }
    }

    private fun parseQuery(query: String): KeyParts {
        if (query.isBlank()) return KeyParts(emptyMap())

        val out = LinkedHashMap<String, String>()
        for (segment in query.split("&")) {
            if(segment.isBlank()) throw TypeJsonInvalidRefQuerySegmentException(segment)

            val (k, v) = segment.split("=", limit = 2).let {
                if (!(it.size == 2 && it[0].isNotBlank())) throw TypeJsonInvalidRefQuerySegmentException(segment)
                it[0] to it[1]
            }

            val key = k
            val value = formUrlDecode(v)

            if (out.putIfAbsent(key, value) != null) throw TypeJsonInvalidRefDuplicateQueryParamException(key)
        }
        return KeyParts(out)
    }

    private fun formUrlDecode(value: String): String =
        URLDecoder.decode(value, Charsets.UTF_8)
}