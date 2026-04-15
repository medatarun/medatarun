package io.medatarun.model.ports.needs

import io.medatarun.model.domain.ModelKey
import io.medatarun.platform.kernel.ResourceLocator

interface ModelImporter {
    fun accept(path: String, resourceLocator: ResourceLocator): Boolean
    fun toModel(path: String, resourceLocator: ResourceLocator, modelKeyChoosen: ModelKey?, modelNameChoosen: String?): ModelImporterData

}