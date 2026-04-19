package io.medatarun.ext.frictionlessdata

import io.medatarun.model.domain.ModelKey
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.model.ports.needs.ModelImporterData
import io.medatarun.platform.kernel.ResourceLocator
import io.medatarun.platform.kernel.Service

class FrictionlessdataModelImporter : ModelImporter, Service {
    val converter = FrictionlessConverter()
    override fun accept(
        path: String,
        resourceLocator: ResourceLocator
    ): Boolean {
        return converter.isCompatible(path, resourceLocator)
    }

    override fun toModel(path: String, resourceLocator: ResourceLocator, modelKeyChoosen: ModelKey?, modelNameChoosen: String?): ModelImporterData {
        return converter.convert(path, resourceLocator, modelKeyChoosen, modelNameChoosen)
    }


}
