package io.medatarun.ext.datamdfile.internal

import io.medatarun.model.model.EntityDefId
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

    fun listEntityDefinitionIds(): Set<EntityDefId> {
        if (!Files.exists(repositoryRoot)) return emptySet()

        return Files.list(repositoryRoot).use { stream ->
            stream
                .filter { Files.isDirectory(it) }
                .map { EntityDefId(it.fileName.toString()) }
                .collect(Collectors.toSet())
        }
    }

    fun listEntityFiles(entityDefId: EntityDefId): List<EntityFile> {
        val entityDir = repositoryRoot.resolve(entityDefId.value)
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

    fun exists(entityDefId: EntityDefId, entityId: String): Boolean =
        Files.exists(entityFilePath(entityDefId, entityId))

    fun read(entityDefId: EntityDefId, entityId: String): MarkdownString =
        MarkdownString(Files.readString(entityFilePath(entityDefId, entityId)))

    fun read(entityFile: EntityFile): MarkdownString = MarkdownString(Files.readString(entityFile.path))

    fun write(entityDefId: EntityDefId, entityId: String, content: MarkdownString): Path {
        val entityDir = ensureEntityDirectory(entityDefId)
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

    fun delete(entityDefId: EntityDefId, entityId: String) {
        Files.deleteIfExists(entityFilePath(entityDefId, entityId))
    }

    private fun ensureEntityDirectory(entityDefId: EntityDefId): Path {
        val entityDir = repositoryRoot.resolve(entityDefId.value)
        Files.createDirectories(entityDir)
        return entityDir
    }

    private fun entityFilePath(entityDefId: EntityDefId, entityId: String): Path =
        repositoryRoot.resolve(entityDefId.value).resolve("$entityId$MARKDOWN_EXTENSION")

    data class EntityFile(val entityId: String, val path: Path)

    companion object {
        private const val MARKDOWN_EXTENSION = ".md"
    }
}
