package io.medatarun.ext.datamdfile.internal

import io.medatarun.model.domain.EntityKey
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors

/**
 * Small helper encapsulating all filesystem interactions for the Markdown repository.
 */
internal class RepositoryFileManager(private val repositoryRoot: Path) {

    init {
        Files.createDirectories(repositoryRoot)
    }

    fun listEntityDefinitionIds(): Set<EntityKey> {
        if (!Files.exists(repositoryRoot)) return emptySet()

        return Files.list(repositoryRoot).use { stream ->
            stream
                .filter { Files.isDirectory(it) }
                .map { EntityKey(it.fileName.toString()) }
                .collect(Collectors.toSet())
        }
    }

    fun listEntityFiles(entityKey: EntityKey): List<EntityFile> {
        val entityDir = repositoryRoot.resolve(entityKey.value)
        if (!Files.exists(entityDir)) return emptyList()

        val files = mutableListOf<EntityFile>()
        Files.newDirectoryStream(entityDir) { path ->
            Files.isRegularFile(path) && path.fileName.toString().endsWith(MARKDOWN_EXTENSION)
        }.use { stream ->
            for (path in stream) {
                val entityId = path.fileName.toString().removeSuffix(MARKDOWN_EXTENSION)
                files += EntityFile(entityId = entityId, path = path)
            }
        }
        return files.sortedBy { it.entityId }
    }

    fun exists(entityKey: EntityKey, entityId: String): Boolean =
        Files.exists(entityFilePath(entityKey, entityId))

    fun read(entityKey: EntityKey, entityId: String): MarkdownString =
        MarkdownString(Files.readString(entityFilePath(entityKey, entityId)))

    fun read(entityFile: EntityFile): MarkdownString = MarkdownString(Files.readString(entityFile.path))

    fun write(entityKey: EntityKey, entityId: String, content: MarkdownString): Path {
        val entityDir = ensureEntityDirectory(entityKey)
        val filePath = entityDir.resolve("$entityId$MARKDOWN_EXTENSION")
        Files.writeString(
            filePath,
            content.value,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
        return filePath
    }

    fun delete(entityKey: EntityKey, entityId: String) {
        Files.deleteIfExists(entityFilePath(entityKey, entityId))
    }

    private fun ensureEntityDirectory(entityKey: EntityKey): Path {
        val entityDir = repositoryRoot.resolve(entityKey.value)
        Files.createDirectories(entityDir)
        return entityDir
    }

    private fun entityFilePath(entityKey: EntityKey, entityId: String): Path =
        repositoryRoot.resolve(entityKey.value).resolve("$entityId$MARKDOWN_EXTENSION")

    data class EntityFile(val entityId: String, val path: Path)

    companion object {
        private const val MARKDOWN_EXTENSION = ".md"
    }
}
