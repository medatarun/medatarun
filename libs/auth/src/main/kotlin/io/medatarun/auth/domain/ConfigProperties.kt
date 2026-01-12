package io.medatarun.auth.domain

import io.medatarun.lang.io.medatarun.lang.config.ConfigPropertyDescription

enum class ConfigProperties(
    override val key: String,
    override val type: String,
    override val defaultValue: String,
    override val description: String
) : ConfigPropertyDescription {
    /**
     * Property to get prefilled secret
     */
    BootstrapSecret(
        "medatarun.auth.bootstrap.secret",
        type = "String",
        defaultValue = "<generated>",
        description = """Bootstrap secret used to obtain the initial administrator access.
In most environments, this value does not need to be set.
If not provided, Medatarun generates a random secret at startup and prints it in the logs.

In environments where startup logs are not accessible (for example when running in containers or managed platforms), you should explicitly set this value. Minimum length is 20 characters."""
    )
}