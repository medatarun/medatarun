package io.medatarun.cli

import io.medatarun.resources.*
import io.medatarun.runtime.AppRuntime
import io.medatarun.runtime.getLogger
import io.medatarun.runtime.internal.AppRuntimeScanner.Companion.MEDATARUN_APPLICATION_DATA_ENV
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AppCLIRunner(private val args: Array<String>, private val runtime: AppRuntime) {

    companion object {
        val logger = getLogger(AppCLIRunner::class)
        private val HELP_FLAGS = setOf("help", "--help", "-h")
    }

    private val resources = AppResources()
    private val resourceRepository = ResourceRepository(resources)
    private val actionCtxFactory = ActionCtxFactory(runtime, resourceRepository)

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
        val rawParameters = parseParameters(args)

        val request = ResourceInvocationRequest(
            resourceName = resourceName,
            functionName = functionName,
            rawParameters = rawParameters
        )

        val result = try {
            resourceRepository.handleInvocation(request, actionCtxFactory.create())
        } catch (exception: ResourceInvocationException) {
            logger.error("Invocation error: ${exception.message}")
            logPayload(exception.payload)
            return
        }

        if (result != null && result is String) {
            logger.cli(result)
        }
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

    private fun logPayload(payload: Map<String, String>) {
        logger.error("" + payload.toString())
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
        val resource = resourceRepository.findDescriptorByIdOptional(resourceId)
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
            "  --${param.name}=<${param.type}>"

        }
        logger.cli(renderedParameters)


    }

    private fun printHelpResource(resourceId: String) {

        val resource = resourceRepository.findDescriptorByIdOptional(resourceId)
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
        val descriptors = resourceRepository.findAllDescriptors().sortedBy { it.name.lowercase() }
        descriptors.forEach { descriptor ->
            logger.cli("  help ${descriptor.name}")
        }
    }

}
