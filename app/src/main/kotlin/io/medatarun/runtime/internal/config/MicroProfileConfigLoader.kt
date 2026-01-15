package io.medatarun.runtime.internal.config

import io.smallrye.config.PropertiesConfigSource
import org.eclipse.microprofile.config.Config
import org.eclipse.microprofile.config.spi.ConfigProviderResolver
import org.eclipse.microprofile.config.spi.ConfigSource
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists

class MicroProfileConfigLoader {

    fun load(applicationDir: Path): Config {

        val builder = ConfigProviderResolver.instance().builder
        builder.addDefaultSources()
        builder.addDiscoveredSources()
        builder.addDiscoveredConverters()

        val sources = ArrayList<ConfigSource>()
        val medatarunPropertiesFile = applicationDir.resolve(MEDATARUN_PROPERTIES_FILE).absolute()
        if (medatarunPropertiesFile.exists()) {
            sources.add(
                PropertiesConfigSource(
                    medatarunPropertiesFile.toUri().toURL(),
                    MEDATARUN_PROPERTIES_ORDINAL
                )
            )
        }

        if (sources.isNotEmpty()) {
            builder.withSources(*sources.toTypedArray())
        }

        return builder.build()
    }


    companion object {
        private const val MEDATARUN_PROPERTIES_FILE = "config/medatarun.properties"

        // Keep project files above classpath defaults, but below env/system properties.
        private const val MEDATARUN_PROPERTIES_ORDINAL = 200


    }
}
