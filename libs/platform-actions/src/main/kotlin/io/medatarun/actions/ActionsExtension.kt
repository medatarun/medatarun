package io.medatarun.actions

import io.medatarun.actions.actions.BatchActionProvider
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx

class ActionsExtension : MedatarunExtension {
    override val id: String = "platform-actions"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".providers", ActionProvider::class)

        ctx.register(ActionProvider::class, BatchActionProvider())

    }
}