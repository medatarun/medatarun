package io.medatarun.platform.telemetry

import io.medatarun.platform.kernel.Service
import io.opentelemetry.api.common.AttributeKey

interface Telemetry: Service  {
    /**
     * Indicates if telemetry is enabled or not
     */
    val enabled: Boolean

    /** Create a new span */
    fun <T> span(name: String, block: (TelemetrySpan) -> T): T
    /** Set or update attribute value */
    fun setAttribute(attribute: AttributeKey<String>, value: String)
    /** Update the current span name */
    fun updateName(value: String)
}

interface TelemetrySpan {
    fun setAttribute(key: String, value: String)
    fun recordException(exception: Throwable)
}