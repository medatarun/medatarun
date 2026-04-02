package io.medatarun.runtime.internal.config

import io.smallrye.config.PropertiesConfigSource
import io.smallrye.config.SmallRyeConfig
import org.eclipse.microprofile.config.spi.ConfigProviderResolver
import org.eclipse.microprofile.config.spi.ConfigSource
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists

class MicroProfileConfigLoader {

    fun load(applicationDir: Path, builtInProperties: Map<String,String>): SmallRyeConfig {

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

        sources.add(PropertiesConfigSource(
            builtInProperties, "built-in-properties", MEDATARUN_BUILTIN_PROPERTIES_ORDINAL
        ))

        if (sources.isNotEmpty()) {
            builder.withSources(*sources.toTypedArray())
        }

        return builder.build().unwrap(SmallRyeConfig::class.java)
    }


    companion object {
        private const val MEDATARUN_PROPERTIES_FILE = "config/medatarun.properties"

        // Keep project files above classpath defaults, but below env/system properties.
        private const val MEDATARUN_PROPERTIES_ORDINAL = 200
        private const val MEDATARUN_BUILTIN_PROPERTIES_ORDINAL = 0


    }
}
