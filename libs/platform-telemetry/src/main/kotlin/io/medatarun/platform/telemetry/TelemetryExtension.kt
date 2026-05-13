package io.medatarun.platform.telemetry

import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.platform.telemetry.internal.OpenTelemetryProvider
import io.medatarun.platform.telemetry.internal.TelemetryOpenTelemetry

class TelemetryExtension : MedatarunExtension {
    override val id: ExtensionId = "platform-telemetry"
    override fun initServices(ctx: MedatarunServiceCtx) {
        val enabled = ctx.getConfigProperty(TelemetryConfigProperties.Enabled.key, "false") == "true"
        val openTelemetryWrapper = TelemetryOpenTelemetry(enabled)
        val openTelemetryProvider = OpenTelemetryProvider(openTelemetryWrapper)

        ctx.register(Telemetry::class, openTelemetryWrapper)
        ctx.register(OpenTelemetryProvider::class, openTelemetryProvider)
    }
}