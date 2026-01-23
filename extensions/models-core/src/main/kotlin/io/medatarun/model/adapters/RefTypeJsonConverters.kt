package io.medatarun.model.adapters

import io.medatarun.types.TypeJsonConverterBadFormatException
import io.medatarun.types.TypeJsonConverterIllegalNullException
import kotlinx.serialization.json.*

object RefTypeJsonConverters {
    fun <R> expectingString(json: JsonElement, block: (value: String) -> R): R {
        return when (json) {
            is JsonNull -> throw TypeJsonConverterIllegalNullException()
            is JsonArray -> throw TypeJsonConverterBadFormatException("expected a JsonObject")
            is JsonObject -> throw TypeJsonConverterBadFormatException("expected a JsonString")
            is JsonPrimitive -> {
                if (json.isString) {
                    block(json.content)
                } else {
                    throw TypeJsonConverterBadFormatException("expected expected a JsonString")
                }
            }
        }
    }

    class KeyParts(val parts:Map<String,String>): Map<String,String> by parts {
        fun required(name: String): String {
            return this[name] ?: throw TypeJsonConverterBadFormatException("Invalid ref, missing $name in key parts.")
        }
    }

    fun <R> decodeRef(json: JsonElement, whenId: (value: String) -> R, whenKey: (params: KeyParts) -> R):R {
        return expectingString(json) {
            decodeRefParts(it, whenId, whenKey)
        }

    }

    fun <R> decodeRefParts(ref: String, whenId: (value: String) -> R, whenKey: (params: KeyParts) -> R): R {
        val (scheme, rest) = ref.split(":", limit = 2).let { splitted ->
            val valid = splitted.size == 2 && splitted[0].isNotBlank() && splitted[1].isNotBlank()
            if (!valid) throw TypeJsonConverterBadFormatException("Invalid ref: $ref")
            splitted[0] to splitted[1]
        }
        return when (scheme) {
            "id" -> whenId(rest)
            "key" -> whenKey(parseQuery(rest))
            else -> throw TypeJsonConverterBadFormatException("Unupported ref scheme: $scheme")
        }
    }

    private fun parseQuery(query: String): KeyParts {
        if (query.isBlank()) return KeyParts(emptyMap())

        val out = LinkedHashMap<String, String>()
        for (segment in query.split("&")) {
            if(segment.isBlank()) throw TypeJsonConverterBadFormatException("Invalid query segment: '$segment'")

            val (k, v) = segment.split("=", limit = 2).let {
                if (!(it.size == 2 && it[0].isNotBlank())) throw TypeJsonConverterBadFormatException("Invalid query segment: '$segment'")
                it[0] to it[1]
            }

            val key = k
            val value = formUrlDecode(v)

            if (out.putIfAbsent(key, value) != null) throw TypeJsonConverterBadFormatException("Duplicate query param: '$key'")
        }
        return KeyParts(out)
    }

    private fun formUrlDecode(value: String): String =
        java.net.URLDecoder.decode(value, Charsets.UTF_8)
}