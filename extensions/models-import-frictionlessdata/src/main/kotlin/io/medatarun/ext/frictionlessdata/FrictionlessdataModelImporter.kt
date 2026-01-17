package io.medatarun.ext.frictionlessdata

import io.medatarun.model.domain.Model
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.platform.kernel.ResourceLocator

class FrictionlessdataModelImporter: ModelImporter {
    val converter = FrictionlessConverter()
    override fun accept(
        path: String,
        resourceLocator: ResourceLocator
    ): Boolean {
        return converter.isCompatible(path, resourceLocator)
    }

    override fun toModel(path: String, resourceLocator: ResourceLocator): Model {
        return converter.readString(path, resourceLocator)
    }


}