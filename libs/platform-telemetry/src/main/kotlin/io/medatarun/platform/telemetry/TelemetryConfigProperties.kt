package io.medatarun.platform.telemetry

import io.medatarun.lang.config.ConfigPropertyDescription

enum class TelemetryConfigProperties(
    override val key: String,
    override val type: String,
    override val defaultValue: String,
    override val description: String
): ConfigPropertyDescription {
    Enabled(
        "medatarun.telemetry.enabled",
        "Boolean",
        "false",
        """Enables OpenTelemetry integration. Default is false. Once enabled, you can configure OpenTelemetry Java configuration to send spans to your observability stack. See https://opentelemetry.io/docs/languages/java/configuration/ and our documentation. Note that we use the zero-code SDK autoconfigure mode, so you can configure with environment variables or system properties."""
    )
}