package io.medatarun.config


import io.medatarun.lang.config.ConfigPropertyDescription

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
    )

}

