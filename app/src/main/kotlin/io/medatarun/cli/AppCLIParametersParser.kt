package io.medatarun.cli

import io.medatarun.httpserver.cli.CliActionDto
import io.medatarun.types.JsonTypeEquiv
import kotlinx.serialization.json.*

class AppCLIParametersParser {
    fun parseParameters(args: Array<String>, action: CliActionDto): JsonObject {


        val parameters = LinkedHashMap<String, String>()
        var index = 0
        while (index < args.size) {
            val argument = args[index]
            if (argument.startsWith("--")) {
                val split = argument.removePrefix("--").split("=", limit = 2)
                if (split.size != 2) {
                    throw CliParameterFormatException(argument)
                }
                val key = split[0]
                val value = split[1]
                if (key.isBlank()) {
                    throw CliParameterFormatException(argument)
                }
                parameters[key] = value
            }
            index++
        }
        val paramByKey = action.parameters.associateBy { it.key }
        for (entry in parameters) {
            if (!paramByKey.containsKey(entry.key)) {
                throw CliParameterUnknownException(entry.key)
            }
        }

        return buildJsonObject {
            for (param in action.parameters) {
                val rawValue = parameters[param.key]
                if (rawValue == null) {
                    if (!param.optional) {
                        throw CliParameterMissingException(param.key)
                    }
                    put(param.key, JsonNull)
                } else {
                    val jsonType = JsonTypeEquiv.valueOfCode(param.jsonType)
                    val jsonValue = parseValue(param.key, rawValue, jsonType)
                    put(param.key, jsonValue)
                }
            }
        }
    }

    private fun parseValue(paramKey: String, rawValue: String, jsonType: JsonTypeEquiv): JsonElement {
        return when (jsonType) {
            JsonTypeEquiv.STRING -> JsonPrimitive(rawValue)
            JsonTypeEquiv.BOOLEAN -> {
                val value = rawValue.toBooleanStrictOrNull()
                    ?: throw CliParameterBooleanValueException(paramKey, rawValue)
                JsonPrimitive(value)
            }
            JsonTypeEquiv.NUMBER -> {
                val value = rawValue.toDoubleOrNull()
                    ?: throw CliParameterNumberValueException(paramKey, rawValue)
                if (!value.isFinite()) {
                    throw CliParameterNumberValueException(paramKey, rawValue)
                }
                JsonPrimitive(value)
            }
            JsonTypeEquiv.OBJECT -> {
                val element = parseJsonValue(paramKey, rawValue, jsonType)
                if (element !is JsonObject) {
                    throw CliParameterObjectValueException(paramKey, rawValue)
                }
                element
            }
            JsonTypeEquiv.ARRAY -> {
                val element = parseJsonValue(paramKey, rawValue, jsonType)
                if (element !is JsonArray) {
                    throw CliParameterArrayValueException(paramKey, rawValue)
                }
                element
            }
        }
    }

    private fun parseJsonValue(paramKey: String, rawValue: String, jsonType: JsonTypeEquiv): JsonElement {
        try {
            return Json.parseToJsonElement(rawValue)
        } catch (exception: Exception) {
            // Provide a MedatarunException for invalid JSON payloads so tests can match on class.
            throw CliParameterJsonParseException(paramKey, jsonType.code, rawValue)
        }
    }
}
