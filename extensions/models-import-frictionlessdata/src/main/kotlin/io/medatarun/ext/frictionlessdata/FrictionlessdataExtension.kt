package io.medatarun.ext.frictionlessdata

import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx

class FrictionlessdataExtension : MedatarunExtension {
    override val id: ExtensionId = "models-import-frictionlessdata"
    override fun initContributions(ctx: MedatarunExtensionCtx) {
        ctx.registerContribution(ModelImporter::class, ctx.getService(FrictionlessdataModelImporter::class))
    }

    override fun initServices(ctx: MedatarunServiceCtx) {
        val importer = FrictionlessdataModelImporter()
        ctx.register(FrictionlessdataModelImporter::class, importer)
    }
}

