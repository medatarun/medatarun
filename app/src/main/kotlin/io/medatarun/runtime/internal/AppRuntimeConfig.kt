package io.medatarun.runtime.internal

import io.medatarun.kernel.MedatarunConfig
import io.medatarun.kernel.ResourceLocator
import org.eclipse.microprofile.config.Config
import java.nio.file.Path

class AppRuntimeConfig(
    override val applicationHomeDir: Path,
    override val projectDir: Path,
    override val medatarunDir: Path,
    public val config: Config,
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
}
