package io.medatarun.ext.frictionlessdata

import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx

class FrictionlessdataExtension : MedatarunExtension {
    override val id: ExtensionId = "frictionlessdata"
    override fun init(ctx: MedatarunExtensionCtx) {
       ctx.register(ModelImporter::class, FrictionlessdataModelImporter())
    }
}