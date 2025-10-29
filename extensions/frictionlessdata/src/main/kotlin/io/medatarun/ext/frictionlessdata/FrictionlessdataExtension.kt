package io.medatarun.ext.frictionlessdata

import io.medatarun.kernel.ExtensionId
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.model.ModelImporter

class FrictionlessdataExtension : MedatarunExtension {
    override val id: ExtensionId = "frictionlessdata"
    override fun init(ctx: MedatarunExtensionCtx) {
       ctx.register(ModelImporter::class, FrictionlessdataModelImporter())
    }
}