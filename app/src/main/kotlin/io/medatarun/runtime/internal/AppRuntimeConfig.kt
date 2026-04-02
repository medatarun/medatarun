package io.medatarun.runtime.internal

import io.medatarun.platform.kernel.MedatarunConfig
import io.medatarun.platform.kernel.ResourceLocator
import io.smallrye.config.SmallRyeConfig
import java.net.URI
import java.nio.file.Path

class AppRuntimeConfig(
    override val applicationHomeDir: Path,
    override val projectDir: Path,
    val serverHost: String,
    val serverPort: Int,
    override val publicBaseURL: URI,
    val config: SmallRyeConfig,
    val resourceLocatorFactory: () -> ResourceLocator
) : MedatarunConfig {

    override fun createResourceLocator(): ResourceLocator {
        return resourceLocatorFactory.invoke()
    }

    override fun getProperty(key: String): String? {
        return config.getOptionalValue(key, String::class.java).orElse(null)
    }

    override fun getProperty(key: String, defaultValue: String): String {
        return config.getOptionalValue(key, String::class.java).orElse(defaultValue)
    }

    override fun getPropertyMapStartingWith(prefix: String): Map<String, String> {
        val mapPrefix = prefix.removeSuffix(".")
        val matchingProperties = linkedMapOf<String, String>()
        val mapValues = config.getOptionalValues(mapPrefix, String::class.java, String::class.java).orElse(emptyMap())
        for (entry in mapValues.entries) {
            val subKey = entry.key
            val value = entry.value
            if (subKey.isNotBlank()) {
                matchingProperties[subKey] = value
            }
        }
        return matchingProperties
    }

}
