package io.medatarun.config

import io.medatarun.runtime.internal.AppRuntimeConfig
import io.medatarun.runtime.internal.AppRuntimeConfigFactory
import java.net.URI


fun createConfig(cli: Boolean): BasicConfig {
    val config = AppRuntimeConfigFactory(cli).create()
    val serverPort: Int = config.getProperty("medatarun.server.port", "8080").toInt()
    val serverHost: String = config.getProperty("medatarun.server.host", "0.0.0.0")

    @Suppress("HttpUrlsUsage")
    val publicBaseUrl: String = config.getProperty("medatarun.public.base.url", "http://$serverHost:$serverPort/")
    return object : BasicConfig {
        override val config: AppRuntimeConfig = config
        override val serverHost: String = serverHost
        override val serverPort: Int = serverPort
        override val publicBaseUrl: URI = URI(publicBaseUrl)

    }
}

interface BasicConfig {
    val config: AppRuntimeConfig
    val serverHost: String
    val serverPort: Int
    val publicBaseUrl: URI
}