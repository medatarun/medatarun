package io.medatarun.runtime.internal

import io.medatarun.ext.modeljson.ModelJsonExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.model.infra.ModelStorageComposite
import io.medatarun.model.internal.ModelCmdImpl
import io.medatarun.model.internal.ModelQueriesImpl
import io.medatarun.model.model.ModelCmd
import io.medatarun.model.model.ModelQueries
import io.medatarun.model.model.ModelRepository
import io.medatarun.runtime.AppRuntime
import java.nio.file.Path

class AppRuntimeBuilder {
    fun build(): AppRuntime {
        val scanner = AppRuntimeScanner()
        val config = scanner.scan()
        val extensions = listOf(
            ModelJsonExtension(),
        )
        val repositories = mutableListOf<ModelRepository>()
        val extensionCtx = object : MedatarunExtensionCtx {
            override fun getConfigProperty(key: String): String? {
                return config.getProperty(key)
            }

            override fun getConfigProperty(key: String, defaultValue: String): String {
                return config.getProperty(key, defaultValue)
            }

            override fun resolveProjectPath(relativePath: String): Path {
                return config.projectDir.resolve(relativePath).toAbsolutePath()
            }

            override fun registerRepository(repo: ModelRepository) {
                repositories.add(repo)
            }

        }
        extensions.forEach { extension -> extension.init(extensionCtx) }
        val storage = ModelStorageComposite(repositories)
        val queries = ModelQueriesImpl(storage)
        val cmd = ModelCmdImpl(storage)
        return object : AppRuntime {
            override val modelCmd: ModelCmd = cmd
            override val modelQueries: ModelQueries = queries

        }
    }
}