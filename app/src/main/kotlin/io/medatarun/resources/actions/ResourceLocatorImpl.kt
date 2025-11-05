package io.medatarun.resources.actions

import io.medatarun.model.ports.ResourceLocator
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path

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
        val targetUri = resolveUri(path)
        return ResourceLocatorImpl(targetUri.toString(), fileSystem)
    }

    override fun resolveUri(path: String): URI {
        val candidate = URI(path)
        return if (candidate.isAbsolute) candidate else baseUri.resolve(candidate)
    }

    private fun readUri(uri: URI): String {
        return when (uri.scheme) {
            "file", null -> {
                logger.info("Reading file from $uri")
                val p: Path = fileSystem.provider().getPath(uri)
                Files.newBufferedReader(p, StandardCharsets.UTF_8).use { it.readText() }
            }

            "http", "https" -> {
                logger.info("Downloading file from $uri")
                val req = HttpRequest.newBuilder(uri).GET().build()
                val resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                if (resp.statusCode() in 200..299) resp.body()
                else throw IllegalStateException("HTTP ${resp.statusCode()} for $uri")
            }

            else -> throw UnsupportedOperationException("Unsupported scheme: ${uri.scheme}")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ResourceLocatorImpl::class.java)
    }
}