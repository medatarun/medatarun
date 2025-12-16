package io.medatarun.model.ports.needs

import io.medatarun.kernel.ResourceLocator
import io.medatarun.model.domain.Model

interface ModelImporter {
    fun accept(path: String, resourceLocator: ResourceLocator): Boolean
    fun toModel(path: String, resourceLocator: ResourceLocator): Model

}