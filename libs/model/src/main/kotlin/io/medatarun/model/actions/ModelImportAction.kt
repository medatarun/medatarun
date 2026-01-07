package io.medatarun.model.actions

import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.kernel.ResourceLocator
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.needs.ModelImporter

class ModelImportAction(
    private val extensionRegistry: ExtensionRegistry,
    private val modelCmds: ModelCmds,
    private val resourceLocator: ResourceLocator
) {

    fun process(cmd: ModelAction.Import) {
        val contribs = extensionRegistry.findContributionsFlat(ModelImporter::class)
        val resourceLocator = resourceLocator.withPath(cmd.from)
        val contrib = contribs.firstOrNull { contrib ->
            contrib.accept(cmd.from, resourceLocator)
        }
        if (contrib == null) {
            throw ModelImportActionNotFoundException(cmd.from)
        }
        val model = contrib.toModel(cmd.from, resourceLocator)
        modelCmds.dispatch(ModelCmd.ImportModel(model))
    }

}

