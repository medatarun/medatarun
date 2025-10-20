package io.medatarun.app.io.medatarun.httpserver.commons

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlin.collections.component1
import kotlin.collections.component2

object HttpAdapters {

    fun jsonObjectToStringMap(jsonObject: JsonObject): Map<String, String> =
        jsonObject.entries.mapNotNull { (key, value) -> toPrimitiveString(value)?.let { key to it } }.toMap()

    fun toPrimitiveString(element: JsonElement): String? = when (element) {
        is JsonPrimitive -> element.contentOrNull ?: element.toString()
        else -> element.toString()
    }
}