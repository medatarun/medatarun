package io.medatarun.resources.actions

import io.medatarun.model.ModelImporter
import io.medatarun.model.model.ModelCmd
import io.medatarun.resources.ModelResourceCmd
import io.medatarun.runtime.AppRuntime
import java.nio.file.FileSystem

class ModelImportAction(
    private val runtime: AppRuntime,
    private val fileSystem: FileSystem
) {

    fun process(cmd: ModelResourceCmd.Import) {
        val contribs = runtime.extensionRegistry.findContributionsFlat(ModelImporter::class)
        val resourceLocator = ResourceLocatorImpl(cmd.from, fileSystem)
        val contrib = contribs.firstOrNull { contrib ->
            contrib.accept(cmd.from, resourceLocator)
        }
        if (contrib == null) {
            throw ModelImportActionNotFoundException(cmd.from)
        }
        val model = contrib.toModel(cmd.from, resourceLocator)
        runtime.modelCmds.dispatch(ModelCmd.ImportModel(model))
    }

}

