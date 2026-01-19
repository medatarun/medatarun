package io.medatarun.model.actions

import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.ResourceLocator

class ModelImportAction(
    private val extensionRegistry: ExtensionRegistry,
    private val modelCmds: ModelCmds,
    private val resourceLocator: ResourceLocator
) {

    fun process(cmd: ModelAction.Import) {

    }

}

