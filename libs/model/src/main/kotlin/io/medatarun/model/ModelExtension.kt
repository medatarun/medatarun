package io.medatarun.model

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.model.actions.ModelActionProvider
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.model.ports.needs.ModelRepository

/**
 * Extension to register the "model" base plugin to the kernel.
 */
class ModelExtension: MedatarunExtension {
    override val id: String = "model"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".repositories", ModelRepository::class)
        ctx.registerContributionPoint(this.id + ".importer", ModelImporter::class)
        ctx.register(ActionProvider::class, ModelActionProvider(ctx.createResourceLocator()))
    }

}
