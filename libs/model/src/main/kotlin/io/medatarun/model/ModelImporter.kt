package io.medatarun.model

import io.medatarun.model.domain.Model
import io.medatarun.model.ports.needs.ResourceLocator

interface ModelImporter {
    fun accept(path: String, resourceLocator: ResourceLocator): Boolean
    fun toModel(path: String, resourceLocator: ResourceLocator): Model

}
