package io.medatarun.model.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.kernel.ResourceLocator
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.needs.ModelImporter

class ModelImportAction(
    private val actionCtx: ActionCtx,
    private val resourceLocator: ResourceLocator
) {

    fun process(cmd: ModelAction.Import) {
        val contribs = actionCtx.extensionRegistry.findContributionsFlat(ModelImporter::class)
        val resourceLocator = resourceLocator.withPath(cmd.from)
        val contrib = contribs.firstOrNull { contrib ->
            contrib.accept(cmd.from, resourceLocator)
        }
        if (contrib == null) {
            throw ModelImportActionNotFoundException(cmd.from)
        }
        val model = contrib.toModel(cmd.from, resourceLocator)
        actionCtx.modelCmds.dispatch(ModelCmd.ImportModel(model))
    }

}

