package io.medatarun.ext.frictionlessdata

import io.medatarun.model.domain.ModelAggregate
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.platform.kernel.ResourceLocator

class FrictionlessdataModelImporter(
    tagImporter: FrictionlessTagImporter
) : ModelImporter {
    val converter = FrictionlessConverter(tagImporter)
    override fun accept(
        path: String,
        resourceLocator: ResourceLocator
    ): Boolean {
        return converter.isCompatible(path, resourceLocator)
    }

    override fun toModel(path: String, resourceLocator: ResourceLocator, modelKey: ModelKey?, modelName: String?): ModelAggregate {
        return converter.readString(path, resourceLocator, modelKey, modelName)
    }


}
