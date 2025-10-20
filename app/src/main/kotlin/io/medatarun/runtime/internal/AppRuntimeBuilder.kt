package io.medatarun.runtime.internal

import io.medatarun.ext.modeljson.ModelJsonExtension
import io.medatarun.kernel.internal.ExtensionPlaformImpl
import io.medatarun.model.ModelExtension
import io.medatarun.model.infra.ModelStorageComposite
import io.medatarun.model.internal.ModelCmdImpl
import io.medatarun.model.internal.ModelQueriesImpl
import io.medatarun.model.model.ModelCmd
import io.medatarun.model.model.ModelQueries
import io.medatarun.model.model.ModelRepository
import io.medatarun.runtime.AppRuntime

class AppRuntimeBuilder {
    fun build(): AppRuntime {
        val scanner = AppRuntimeScanner()
        val config = scanner.scan()
        val extensions = listOf(
            ModelExtension(),
            ModelJsonExtension(),
        )
        val platform = ExtensionPlaformImpl(extensions, config)
        val repositories = platform.extensionRegistry.findContributionsFlat(ModelRepository::class)
        val storage = ModelStorageComposite(repositories)
        val queries = ModelQueriesImpl(storage)
        val cmd = ModelCmdImpl(storage)
        return object : AppRuntime {
            override val modelCmd: ModelCmd = cmd
            override val modelQueries: ModelQueries = queries

        }
    }
}