package io.medatarun.cli

import io.medatarun.app.io.medatarun.resources.ResourceInvocationException
import io.medatarun.app.io.medatarun.resources.ResourceInvocationRequest
import io.medatarun.app.io.medatarun.resources.ResourceRepository
import io.medatarun.runtime.getLogger

class AppCLIRunner(private val args: Array<String>, private val resources: AppCLIResources) {

    companion object {
        val logger = getLogger(AppCLIRunner::class)
    }

    private val resourceRepository = ResourceRepository(resources)

    init {
        logger.debug("Called with arguments: ${args.joinToString(" ")}")
    }

    fun handleCLI() {
        if (args.size < 2) {
            logger.error("Usage: app <resource> <function> [--param valeur]")
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
}
