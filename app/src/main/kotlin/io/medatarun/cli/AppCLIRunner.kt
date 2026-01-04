package io.medatarun.cli

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.httpserver.cli.CliActionGroupDto
import io.medatarun.runtime.getLogger
import io.medatarun.runtime.internal.AppRuntimeScanner.Companion.MEDATARUN_APPLICATION_DATA_ENV
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AppCLIRunner(
    private val args: Array<String>,
    private val defaultServerPort: Int,
    private val defaultServerHost: String,
) {

    companion object {
        val logger = getLogger(AppCLIRunner::class)
        private val HELP_FLAGS = setOf("help", "--help", "-h")
    }


    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        expectSuccess = false
    }
    private var actionRegistryCache: List<CliActionGroupDto>? = null

    init {
        logger.debug("Called with arguments: ${args.joinToString(" ")}")
    }

    fun handleCLI() {
        if (args.isEmpty() || args[0] in HELP_FLAGS) {
            printHelp(args.getOrNull(1), args.getOrNull(2))
            return
        }

        if (args.size < 2) {
            logger.error("Usage: app <resource> <function> [--param valeur]")
            printHelp()
            return
        }

        val resourceName = args[0]
        val functionName = args[1]
        val resource = findResource(resourceName)
        if (resource == null) {
            logger.error("Resource not found: $resourceName")
            printHelpRoot()
            return
        }
        val commandExists = resource.commands.any { it.name == functionName }
        if (!commandExists) {
            logger.error("Command not found: $resourceName $functionName")
            printHelpResource(resourceName)
            return
        }
        val rawParameters = parseParameters(args)

        val request = ActionRequest(
            group = resourceName,
            command = functionName,
            payload = rawParameters
        )

        val result = runBlocking {
            invokeRemoteAction(request)
        }

        if (result.isNotBlank()) {
            logger.cli(result)
        }
    }

    private suspend fun invokeRemoteAction(request: ActionRequest): String {
        val url = "http://${defaultServerHost}:${defaultServerPort}/api/${request.group}/${request.command}"
        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(request.payload)
        }
        val responseBody = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw RemoteActionInvocationException(
                "Remote invocation failed with status ${response.status.value}: $responseBody"
            )
        }
        return responseBody
    }

    private fun loadActionRegistry(): List<CliActionGroupDto> {
        val cached = actionRegistryCache
        if (cached != null) {
            return cached
        }
        val registry = runBlocking { fetchActionRegistry() }
        actionRegistryCache = registry
        return registry
    }

    private suspend fun fetchActionRegistry(): List<CliActionGroupDto> {
        val url = "http://${defaultServerHost}:${defaultServerPort}/cli/api/action-registry"
        val response = httpClient.get(url)
        if (!response.status.isSuccess()) {
            val responseBody = response.bodyAsText()
            throw RemoteActionRegistryException(
                "Remote action registry fetch failed with status ${response.status.value}: $responseBody"
            )
        }
        return response.body()
    }

    private fun parseParameters(args: Array<String>): JsonObject {
        val parameters = LinkedHashMap<String, String>()
        var index = 2
        while (index < args.size) {
            val argument = args[index]
            if (argument.startsWith("--")) {
                val split = argument.removePrefix("--").split("=", limit = 2)
                val key = split[0]
                val value = if (split.size == 2) {
                    split[1]
                } else {
                    args.getOrNull(index + 1)?.takeIf { !it.startsWith("--") }
                }

                if (value != null) {
                    parameters[key] = value
                    if (split.size == 1) index++
                }
            }
            index++
        }
        return buildJsonObject {
            for (entry in parameters) {
                put(entry.key, entry.value)
            }
        }
    }

    private fun printHelp(resource: String? = null, command: String? = null) {
        if (resource != null && command != null) {
            printHelpCommand(resource, command)
        } else if (resource != null) {
            printHelpResource(resource)
        } else {
            printHelpRoot()
        }
    }

    private fun printHelpCommand(resourceId: String, commandId: String) {
        val resource = findResource(resourceId)
        if (resource == null) {
            logger.error("Resource not found: $resourceId")
            return printHelpRoot()
        }
        val command = resource.commands.find { it.name == commandId }
        if (command == null) {
            logger.error("Command not found: $resourceId $commandId")
            return printHelpResource(resourceId)
        }



        logger.cli("")
        logger.cli("Resource: $resourceId")
        logger.cli("Command: $commandId")
        logger.cli("")
        command.title?.let { logger.cli("  " + it) }
        logger.cli("")
        command.description?.let { logger.cli("  " + it) }
        logger.cli("")
        val renderedParameters = command.parameters.joinToString("\n") { param ->
            "  --${param.name}=<${param.multiplatformType}>"

        }
        logger.cli(renderedParameters)


    }

    private fun printHelpResource(resourceId: String) {

        val resource = findResource(resourceId)
        if (resource == null) {
            logger.error("Resource not found: $resourceId")
            printHelpRoot()
        } else {
            logger.cli("Get help on available commands: help $resourceId <commandName>")
            val allCommands = resource.commands.sortedBy { it.name.lowercase() }
            val maxKeySize = allCommands.map { it.name }.maxByOrNull { it.length }?.length ?: 0
            allCommands.forEach { command ->
                logger.cli(command.name.padEnd(maxKeySize) + ": " + command.title?.ifBlank { "" })
            }
        }
    }

    private fun printHelpRoot() {
        logger.cli("Usages:")
        logger.cli("  medatarun serve")
        logger.cli("    Launches a medatarun server you can interact with using UI, MCP or API")
        logger.cli("  medatarun <resource> <command> [...parameters]")
        logger.cli("    CLI version of medatarun. Executes specified resource's command.")
        logger.cli("    See below for available resources and their commands.")
        logger.cli("  medatarun help")
        logger.cli("    Display this help")
        logger.cli("  medatarun help <resource>")
        logger.cli("    Display available commands for this resource")
        logger.cli("  medatarun help <resource> <command>")
        logger.cli("    Display command description and parameters for this resource")
        logger.cli("")
        logger.cli("Unless environment variable $MEDATARUN_APPLICATION_DATA_ENV points to a directory, the current directory is considered to be the projet root.")
        logger.cli("")
        logger.cli("Get help on available resources:")
        val descriptors = loadActionRegistry().sortedBy { it.name.lowercase() }
        descriptors.forEach { descriptor ->
            logger.cli("  help ${descriptor.name}")
        }
    }

    private fun findResource(resourceId: String): CliActionGroupDto? {
        val descriptors = loadActionRegistry()
        return descriptors.find { it.name == resourceId }
    }

}
