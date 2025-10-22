package io.medatarun.cli

import io.medatarun.resources.ResourceInvocationException
import io.medatarun.resources.ResourceInvocationRequest
import io.medatarun.resources.ResourceRepository
import io.medatarun.runtime.getLogger

class AppCLIRunner(private val args: Array<String>, private val resources: AppCLIResources) {

    companion object {
        val logger = getLogger(AppCLIRunner::class)
        private val HELP_FLAGS = setOf("help", "--help", "-h")
    }

    private val resourceRepository = ResourceRepository(resources)

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
            resourceRepository.handleInvocation(request)
        } catch (exception: ResourceInvocationException) {
            logger.error("Invocation error: ${exception.message}")
            logPayload(exception.payload)
            return
        }

        if (result != null && result is String) {
            logger.cli(result)
        }
    }

    private fun parseParameters(args: Array<String>): Map<String, String> {
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
        return parameters
    }

    private fun logPayload(payload: Map<String, String>) {
        payload["usage"]?.let { logger.error(it) }
        payload["details"]?.let { logger.error(it) }
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
        logger.cli("  $resourceId $commandId ")
        logger.cli("")
        command.title?.let  { logger.cli("  " + it) }
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
            val allCommands = resource.commands.sortedBy { it.name }
            val maxKeySize = allCommands.map { it.name }.maxBy { it.length }?.length ?: 0
            allCommands.forEach { command ->
                logger.cli(command.name.padEnd(maxKeySize) + ": " + command.title?.ifBlank { "" })
            }
        }
    }

    private fun printHelpRoot() {
        logger.cli("Get help on available resources:")
        val descriptors = resourceRepository.findAllDescriptors().sortedBy { it.name }
        descriptors.forEach { descriptor ->
            logger.cli("  help ${descriptor.name}")
        }
    }

    private fun buildCommandSignature(resourceName: String, command: ResourceRepository.ResourceCommand): String {
        if (command.parameters.isEmpty()) {
            return "$resourceName ${command.name}"
        }
        val renderedParameters = command.parameters.joinToString(" ") { param ->
            val rendered = "--${param.name}=<${param.type}>"
            if (param.optional) "[${rendered}]" else rendered
        }
        return "$resourceName ${command.name} $renderedParameters"
    }
}
