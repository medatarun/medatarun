package io.medatarun.model

import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.model.model.ModelRepository

class ModelExtension: MedatarunExtension {
    override val id: String = "model"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".repositories", ModelRepository::class)
    }

}