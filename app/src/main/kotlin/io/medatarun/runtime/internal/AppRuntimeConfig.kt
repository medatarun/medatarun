package io.medatarun.runtime.internal

import io.medatarun.kernel.MedatarunConfig
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path

class AppRuntimeConfig(
    override val projectDir: Path,
    val config: JsonObject
) : MedatarunConfig {
    override fun getProperty(key: String): String? {
        return config[key]?.jsonPrimitive?.content
    }

    override fun getProperty(key: String, defaultValue: String): String {
        return getProperty(key) ?: defaultValue
    }
}