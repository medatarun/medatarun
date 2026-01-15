package io.medatarun.config

import io.medatarun.lang.io.medatarun.lang.config.ConfigPropertyDescription
import io.medatarun.runtime.internal.AppRuntimeConfig
import io.medatarun.runtime.internal.AppRuntimeConfigFactory
import java.net.URI

enum class AppConfigProperties(
    override val key: String,
    override val type: String,
    override val defaultValue: String,
    override val description: String
) : ConfigPropertyDescription {
    ServerHost(
        "medatarun.server.host",
        "String",
        "0.0.0.0",
        "Hostname or IP address the server binds to."
    ),
    ServerPort(
        "medatarun.server.port",
        "Integer",
        "8080",
        "TCP port the server listens on."
    ),
    BaseUrl(
        "medatarun.public.base.url",
        "String",
        "<generated>",
        """Public base URL of the Medatarun instance.
This is the externally visible URL used for generated links and redirects.
Override it when Medatarun is deployed behind a reverse proxy or accessed via a different hostname.
If not set, it is derived from the server host and port (http://<host>:<port>)."""
    ),
    AuthToken(
        key ="medatarun.auth.token",
        type = "String",
        defaultValue="<none>",
        description = "Authentication token used by the CLI when connecting to a Medatarun instance."
    )

}

fun createConfig(cli: Boolean): BasicConfig {
    val config = AppRuntimeConfigFactory(cli).create()
    val serverPort: Int =
        config.getProperty(AppConfigProperties.ServerPort.key, AppConfigProperties.ServerPort.defaultValue).toInt()
    val serverHost: String =
        config.getProperty(AppConfigProperties.ServerHost.key, AppConfigProperties.ServerHost.defaultValue)
    val serverHostPublicBaseUrlDefault = config.getProperty(AppConfigProperties.ServerHost.key, "localhost")

    @Suppress("HttpUrlsUsage")
    val publicBaseUrl: String = config.getProperty(AppConfigProperties.BaseUrl.key, "http://$serverHostPublicBaseUrlDefault:$serverPort/")
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