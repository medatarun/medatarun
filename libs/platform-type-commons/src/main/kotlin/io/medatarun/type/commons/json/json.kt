package io.medatarun.type.commons.json

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI


fun JsonObject.getStringOrNull(key: String): String? = this.get(key)?.jsonPrimitive?.content
fun JsonObject.getStringListOrNull(key: String): List<String>? = this.get(key)?.jsonArray?.map { it.jsonPrimitive.content }
fun JsonObject.getURIOrNull(key: String): URI? = this.getStringOrNull(key)?.let { URI(it)}
fun JsonObject.getURIListOrNull(key: String): List<URI>? = this.getStringListOrNull(key)?.map { URI(it)}