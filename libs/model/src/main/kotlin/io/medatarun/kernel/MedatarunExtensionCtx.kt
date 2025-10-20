package io.medatarun.kernel

import io.medatarun.model.model.ModelRepository
import java.nio.file.Path

interface MedatarunExtensionCtx {
    fun getConfigProperty(key: String): String?
    fun getConfigProperty(key: String, defaultValue: String): String
    fun resolveProjectPath(relativePath: String): Path
    fun registerRepository(repo: ModelRepository)
}