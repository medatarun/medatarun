package io.medatarun.runtime.internal

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path

class AppRuntimeConfig(
    val projectDir: Path,
    val config: JsonObject

) {
    fun getProperty(key: String): String? {
        return config[key]?.jsonPrimitive?.content
    }
    fun getProperty(key: String, defaultValue: String): String {
        return getProperty(key) ?: defaultValue
    }
}