package io.medatarun.runtime.internal

import io.medatarun.kernel.MedatarunConfig
import io.medatarun.kernel.ResourceLocator
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path

class AppRuntimeConfig(
    override val applicationHomeDir: Path,
    override val projectDir: Path,
    override val medatarunDir: Path,
    val config: JsonObject,
    val resourceLocatorFactory: () -> ResourceLocator,
) : MedatarunConfig {

    override fun createResourceLocator(): ResourceLocator {
        return resourceLocatorFactory.invoke()
    }

    override fun getProperty(key: String): String? {
        return config[key]?.jsonPrimitive?.content
    }

    override fun getProperty(key: String, defaultValue: String): String {
        return getProperty(key) ?: defaultValue
    }
}