package io.medatarun.model

import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.model.ports.ModelRepository

/**
 * Extension to register the "model" base plugin to the kernel.
 */
class ModelExtension: MedatarunExtension {
    override val id: String = "model"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".repositories", ModelRepository::class)
    }
}
