package io.medatarun.ext.frictionlessdata

import io.medatarun.model.ModelImporter
import io.medatarun.model.domain.Model
import io.medatarun.model.ports.needs.ResourceLocator

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