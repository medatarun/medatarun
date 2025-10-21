package io.medatarun.ext.datamdfile.internal

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path

/**
 * Creates an in-memory filesystem based on Jimfs for tests and exposes the key directories used by the extension.
 */
internal class FilesystemBuilder {
    private val fileSystem: FileSystem = Jimfs.newFileSystem(Configuration.unix())
    private val modelsDirectory: Path = Files.createDirectories(fileSystem.getPath("/app/medatarun/models"))
    private val storageDirectory: Path = Files.createDirectories(fileSystem.getPath("/data/medatarun/storage"))

    fun filesystem(): FileSystem = fileSystem

    fun modelsDirectory(): Path = modelsDirectory

    fun storageDirectory(): Path = storageDirectory
}
