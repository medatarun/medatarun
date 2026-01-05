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
import io.medatarun.runtime.internal.AppRuntimeConfigFactory.Companion.MEDATARUN_APPLICATION_DATA_ENV
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AppCLIRunner(
    private val args: Array<String>,
    private val defaultServerPort: Int,
    private val defaultServerHost: String,
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AppCLIRunner::class.java)
        private val HELP_FLAGS = setOf("help", "--help", "-h")
    }

    val parser = AppCLIParametersParser()


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
            logger.error("Usage: app <group> <command> [--param valeur]")
            printHelp()
            return
        }

        val actionGroupKey = args[0]
        val actionKey = args[1]
        val actionGroup = findActionGroup(actionGroupKey)
        if (actionGroup == null) {
            logger.error("Action group not found: $actionGroupKey")
            printHelpRoot()
            return
        }
        val action = actionGroup.actions.firstOrNull{it.key == actionKey}
        val commandExists = action!=null
        if (!commandExists) {
            logger.error("Action not found: $actionGroupKey $actionKey")
            printHelpResource(actionGroupKey)
            return
        }
        val rawParameters = parser.parseParameters(args, action)

        val request = ActionRequest(
            group = actionGroupKey,
            command = actionKey,
            payload = rawParameters
        )

        val result = runBlocking {
            invokeRemoteAction(request)
        }

        if (result.isNotBlank()) {
            logger.info(result)
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
        val resource = findActionGroup(resourceId)
        if (resource == null) {
            logger.error("Group not found: $resourceId")
            return printHelpRoot()
        }
        val command = resource.actions.find { it.key == commandId }
        if (command == null) {
            logger.error("Command not found: $resourceId $commandId")
            return printHelpResource(resourceId)
        }



        logger.info("")
        logger.info("Group  : $resourceId")
        logger.info("Command: $commandId")
        logger.info("")
        command.title?.let { logger.info("  " + it) }
        logger.info("")
        command.description?.let { logger.info("  " + it) }
        logger.info("")
        val renderedParameters = command.parameters.joinToString("\n") { param ->
            "  --${param.key}=<${param.multiplatformType}>"

        }
        logger.info(renderedParameters)


    }

    private fun printHelpResource(resourceId: String) {

        val resource = findActionGroup(resourceId)
        if (resource == null) {
            logger.error("Group not found: $resourceId")
            printHelpRoot()
        } else {
            logger.info("Get help on available commands: help $resourceId <commandName>")
            val allCommands = resource.actions.sortedBy { it.key.lowercase() }
            val maxKeySize = allCommands.map { it.key }.maxByOrNull { it.length }?.length ?: 0
            allCommands.forEach { command ->
                logger.info(command.key.padEnd(maxKeySize) + ": " + command.title?.ifBlank { "" })
            }
        }
    }

    private fun printHelpRoot() {
        logger.info("Usages:")
        logger.info("  medatarun serve")
        logger.info("    Launches a medatarun server you can interact with using UI, MCP or API")
        logger.info("  medatarun <resource> <command> [...parameters]")
        logger.info("    CLI version of medatarun. Executes specified resource's command.")
        logger.info("    See below for available resources and their commands.")
        logger.info("  medatarun help")
        logger.info("    Display this help")
        logger.info("  medatarun help <resource>")
        logger.info("    Display available commands for this resource")
        logger.info("  medatarun help <resource> <command>")
        logger.info("    Display command description and parameters for this resource")
        logger.info("")
        logger.info("Unless environment variable $MEDATARUN_APPLICATION_DATA_ENV points to a directory, the current directory is considered to be the projet root.")
        logger.info("")
        logger.info("Get help on available groups:")
        val descriptors = loadActionRegistry().sortedBy { it.name.lowercase() }
        descriptors.forEach { descriptor ->
            logger.info("  help ${descriptor.name}")
        }
    }

    private fun findActionGroup(resourceId: String): CliActionGroupDto? {
        val descriptors = loadActionRegistry()
        return descriptors.find { it.name == resourceId }
    }


}
