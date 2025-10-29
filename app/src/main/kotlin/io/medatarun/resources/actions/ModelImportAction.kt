package io.medatarun.resources.actions

import io.medatarun.model.ModelImporter
import io.medatarun.model.ports.ResourceLocator
import io.medatarun.resources.ModelResourceCmd
import io.medatarun.runtime.AppRuntime
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path

class ModelImportAction(
    private val runtime: AppRuntime,
    private val fileSystem: FileSystem
) {

    fun process(cmd: ModelResourceCmd.Import) {
        val contribs = runtime.extensionRegistry.findContributionsFlat(ModelImporter::class)
        val resourceLocator = ResourceLocatorImpl(cmd.from, fileSystem)
        contribs.forEach { contrib ->
            val model = contrib.toModel(cmd.from, resourceLocator)
            runtime.modelCmds.importModel(model)
        }

    }

}

class ResourceLocatorImpl(
    private val rootPath: String,
    private val fileSystem: FileSystem
) : ResourceLocator {

    private val baseUri: URI = URI(rootPath).let { uri ->
        if (!uri.isAbsolute) fileSystem.getPath(rootPath).toUri() else uri
    }

    private val httpClient = HttpClient.newHttpClient()

    override fun getRootContent(): String {
        return readUri(baseUri)
    }

    override fun getContent(path: String): String {
        val targetUri = resolveUri(path)
        return readUri(targetUri)
    }

    override fun withPath(path: String): ResourceLocator {
        return ResourceLocatorImpl(path, fileSystem)
    }

    private fun resolveUri(path: String): URI {
        val candidate = URI(path)
        return if (candidate.isAbsolute) candidate else baseUri.resolve(candidate)
    }

    private fun readUri(uri: URI): String {
        return when (uri.scheme) {
            "file", null -> {
                val p: Path = fileSystem.provider().getPath(uri)
                Files.newBufferedReader(p, StandardCharsets.UTF_8).use { it.readText() }
            }

            "http", "https" -> {
                val req = HttpRequest.newBuilder(uri).GET().build()
                val resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                if (resp.statusCode() in 200..299) resp.body()
                else throw IllegalStateException("HTTP ${resp.statusCode()} for $uri")
            }

            else -> throw UnsupportedOperationException("Unsupported scheme: ${uri.scheme}")
        }
    }
}