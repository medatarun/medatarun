package io.medatarun.ext.modeljson

import io.medatarun.model.domain.ModelKey
import java.nio.file.Path
import kotlin.io.path.*

class ModelsJsonStorageFiles(
    private val repositoryPath: Path
) {
    fun getAllModelFiles(): Map<ModelKey, Path> {
        if (!repositoryPath.isDirectory())
            throw ModelJsonRepositoryException("Model repository [$repositoryPath] doesn't exist or is not a repository")

        val paths = repositoryPath
            .listDirectoryEntries("*.json")
            .filter { it.isRegularFile() }
            .map { it.toAbsolutePath().normalize() }

        return paths.associateBy { path ->
            val key = path.fileName.toString().removeSuffix(".json")
            ModelKey(key)
        }
    }

    fun save(key: String, jsonAsString: String): Path {
        val path = repositoryPath.resolve("$key.json")
        path.writeText(jsonAsString)
        return path.toAbsolutePath().normalize()
    }

    fun load(key: ModelKey): String {
        val path = repositoryPath.resolve("$key.json")
        return path.readText()
    }
}