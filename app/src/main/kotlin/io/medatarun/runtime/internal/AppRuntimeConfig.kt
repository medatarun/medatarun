package io.medatarun.runtime.internal

import io.medatarun.platform.kernel.MedatarunConfig
import io.medatarun.platform.kernel.ResourceLocator
import org.eclipse.microprofile.config.Config
import org.slf4j.LoggerFactory
import java.nio.file.Path

class AppRuntimeConfig(
    override val applicationHomeDir: Path,
    override val projectDir: Path,
    val config: Config,
    val resourceLocatorFactory: () -> ResourceLocator,
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

    init {
        val logger = LoggerFactory.getLogger("CONFIG")
        config.propertyNames.sorted().forEach {
            logger.info(it + "=" + config.getOptionalValue(it, String::class.java))
        }
    }
}
