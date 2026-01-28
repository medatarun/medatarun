package io.medatarun.cli

import io.medatarun.lang.config.ConfigPropertyDescription


enum class AppCLIConfigProperty(
    override val key: String,
    override val type: String,
    override val defaultValue: String,
    override val description: String
): ConfigPropertyDescription {
    AuthToken(
        key = "medatarun.auth.token",
        type = "String",
        defaultValue = "<none>",
        description = """Authentication token used by the CLI when connecting to a Medatarun instance."""
    )
}