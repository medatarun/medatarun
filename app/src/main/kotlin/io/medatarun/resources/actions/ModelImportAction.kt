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
        contribs.forEach { contrib ->
            val model = contrib.toModel(cmd.from, resourceLocator)
            runtime.modelCmds.dispatch(ModelCmd.ImportModel(model))
        }

    }

}

