package io.medatarun.model

import io.medatarun.model.model.Model
import io.medatarun.model.ports.ResourceLocator

interface ModelImporter {
    fun toModel(path: String, resourceLocator: ResourceLocator): Model

}
