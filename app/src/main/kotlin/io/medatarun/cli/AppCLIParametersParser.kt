package io.medatarun.cli

import io.medatarun.httpserver.cli.CliActionDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AppCLIParametersParser {
    fun parseParameters(args: Array<String>, action: CliActionDto): JsonObject {
        val parameters = LinkedHashMap<String, String>()
        var index = 2
        while (index < args.size) {
            val argument = args[index]
            if (argument.startsWith("--")) {
                val split = argument.removePrefix("--").split("=", limit = 2)
                val key = split[0]
                val value = if (split.size == 2) {
                    split[1]
                } else {
                    args.getOrNull(index + 1)?.takeIf { !it.startsWith("--") }
                }

                if (value != null) {
                    parameters[key] = value
                    if (split.size == 1) index++
                }
            }
            index++
        }
        return buildJsonObject {
            for (entry in parameters) {
                put(entry.key, entry.value)
            }
        }
    }

}