package io.medatarun.platform.telemetry.internal

import io.medatarun.platform.kernel.Service
import io.medatarun.platform.telemetry.Telemetry
import io.medatarun.platform.telemetry.TelemetrySpan
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk

fun getOpenTelemetry(): OpenTelemetry {
    return AutoConfiguredOpenTelemetrySdk.builder()
        .addPropertiesSupplier {
            mapOf("otel.service.name"  to "medatarun")
        }.build().openTelemetrySdk
}


class TelemetryOpenTelemetry(telemetryEnabled: Boolean) : Telemetry {
    val openTelemetry: OpenTelemetry = if (telemetryEnabled) getOpenTelemetry() else OpenTelemetry.noop()
    override val enabled: Boolean = telemetryEnabled

    override fun <T> span(name: String, block: (TelemetrySpan) -> T): T {
        val span = openTelemetry.getTracer(name)
            .spanBuilder(name)
            .startSpan()
        try {
            span.makeCurrent().use {
                return block(TelemetrySpanOpenTelemetry(span))
            }
        } catch (exception: Exception) {
            span.recordException(exception)
            throw exception
        } finally {
            span.end()
        }
    }


    override fun setAttribute(
        attribute: AttributeKey<String>,
        value: String
    ) {
        Span.current().setAttribute(attribute, value)
    }

    override fun updateName(value: String) {
        Span.current().updateName(value)
    }
}

class TelemetrySpanOpenTelemetry(private val span: Span) : TelemetrySpan {
    override fun setAttribute(key: String, value: String) {
        span.setAttribute(key, value)
    }

    override fun recordException(exception: Throwable) {
        span.recordException(exception)
    }
}

class OpenTelemetryProvider(private val telemetry: TelemetryOpenTelemetry) : Service {
    fun openTelemetry(): OpenTelemetry {
        return telemetry.openTelemetry
    }
}
