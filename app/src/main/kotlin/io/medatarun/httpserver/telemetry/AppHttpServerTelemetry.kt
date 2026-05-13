package io.medatarun.httpserver.telemetry

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.medatarun.actions.runtime.AppHttpServerServices
import io.opentelemetry.instrumentation.ktor.v3_0.KtorServerTelemetry

/**
 * Install OpenTelemetry into Ktor so that http requests are converted to spans.
 * Rewrite the route because sometimes Ktor add intermediates nodes that appear
 * with wrong paths on the backend. So instead of `{api authenticateJwt}/api/{actionGroup}/{actionKey}`
 * we have `/api/models/create`
 */
fun Application.installTelemetry(service: AppHttpServerServices) {
    if (!service.telemetry.enabled) return
    install(KtorServerTelemetry) {
        setOpenTelemetry(service.openTelemetryProvider.openTelemetry())
        attributesExtractor {
            onEnd {
                val path = request.path()
                if (path.startsWith("/api")) {
                    attributes.put("http.route", path)
                }
            }
        }
    }
}