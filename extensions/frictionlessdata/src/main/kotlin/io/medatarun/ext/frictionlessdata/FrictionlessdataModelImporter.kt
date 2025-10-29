package io.medatarun.ext.frictionlessdata

import io.medatarun.model.ModelImporter
import io.medatarun.model.model.Model
import io.medatarun.model.ports.ResourceLocator

class FrictionlessdataModelImporter: ModelImporter {
    val converter = FrictionlessConverter()
    override fun toModel(path: String, resourceLocator: ResourceLocator): Model {
        return converter.readString(path, resourceLocator)
    }


}