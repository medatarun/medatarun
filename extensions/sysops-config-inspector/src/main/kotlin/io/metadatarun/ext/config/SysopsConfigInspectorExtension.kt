package io.metadatarun.ext.config

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.metadatarun.ext.config.actions.ConfigActionProvider

class SysopsConfigInspectorExtension : MedatarunExtension {

    override val id = "sysops-config-inspector"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.register(ActionProvider::class, ConfigActionProvider())
    }

}