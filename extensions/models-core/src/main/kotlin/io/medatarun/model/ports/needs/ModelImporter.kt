package io.medatarun.model.ports.needs

import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelKey
import io.medatarun.platform.kernel.ResourceLocator

interface ModelImporter {
    fun accept(path: String, resourceLocator: ResourceLocator): Boolean
    fun toModel(path: String, resourceLocator: ResourceLocator, modelKey: ModelKey?, modelName: String?): Model

}