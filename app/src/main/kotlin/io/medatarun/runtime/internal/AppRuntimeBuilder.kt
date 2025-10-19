package io.medatarun.runtime.internal

import io.medatarun.model.infra.ModelJsonConverter
import io.medatarun.model.infra.ModelJsonRepository
import io.medatarun.model.infra.ModelStorageComposite
import io.medatarun.model.internal.ModelCmdImpl
import io.medatarun.model.internal.ModelQueriesImpl
import io.medatarun.model.model.ModelCmd
import io.medatarun.model.model.ModelQueries
import io.medatarun.runtime.AppRuntime

class AppRuntimeBuilder {
    fun build(): AppRuntime {
        val scanner = AppRuntimeScanner()
        val config = scanner.scan()
        val storage =
            ModelStorageComposite(listOf(
                ModelJsonRepository(config.modelJsonRepositoryPath, ModelJsonConverter()))
            )
        val queries = ModelQueriesImpl(storage)
        val cmd = ModelCmdImpl(storage)
        return object : AppRuntime {
            override val modelCmd: ModelCmd = cmd
            override val modelQueries: ModelQueries = queries

        }
    }
}