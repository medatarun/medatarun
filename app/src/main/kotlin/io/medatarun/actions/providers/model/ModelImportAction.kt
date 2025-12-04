package io.medatarun.actions.providers.model

import io.medatarun.actions.runtime.ActionCtx
import io.medatarun.model.ModelImporter
import io.medatarun.model.model.ModelCmd
import io.medatarun.runtime.internal.ResourceLocatorImpl
import java.nio.file.FileSystem

class ModelImportAction(
    private val actionCtx: ActionCtx,
    private val fileSystem: FileSystem
) {

    fun process(cmd: ModelAction.Import) {
        val contribs = actionCtx.extensionRegistry.findContributionsFlat(ModelImporter::class)
        val resourceLocator = ResourceLocatorImpl(cmd.from, fileSystem)
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

