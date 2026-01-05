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
    private var actionRegistryCache: AppCLIActionRegistry? = null

    init {
        logger.debug("Called with arguments: ${args.joinToString(" ")}")
    }

    fun handleCLI() {
        val reg = loadActionRegistry()
        val help = AppCLIHelp(reg)

        if (args.isEmpty() || args[0] in HELP_FLAGS) {
            help.printHelp(args.getOrNull(1), args.getOrNull(2))
            return
        }

        if (args.size < 2) {
            logger.error("Usage: app <group> <command> [--param valeur]")
            help.printHelp()
            return
        }

        val actionGroupKey = args[0]
        val actionKey = args[1]
        val actionGroup = reg.findActionGroup(actionGroupKey)
        if (actionGroup == null) {
            logger.error("Action group not found: $actionGroupKey")
            help.printHelpRoot()
            return
        }
        val action = actionGroup.actions.firstOrNull { it.actionKey == actionKey }
        val commandExists = action != null
        if (!commandExists) {
            logger.error("Action not found: $actionGroupKey $actionKey")
            help.printHelpResource(actionGroupKey)
            return
        }
        // Parser expects only parameter flags because group/command are handled here.
        val parameterArgs = args.copyOfRange(2, args.size)
        val rawParameters = parser.parseParameters(parameterArgs, action)

        val request = ActionRequest(
            actionGroupKey = actionGroupKey,
            actionKey = actionKey,
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
        val url = "http://${defaultServerHost}:${defaultServerPort}/api/${request.actionGroupKey}/${request.actionKey}"
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

    private fun loadActionRegistry(): AppCLIActionRegistry {
        val cached = actionRegistryCache
        if (cached != null) {
            return cached
        }
        val registry = runBlocking { fetchActionRegistry() }
        val reg = AppCLIActionRegistry(registry)
        actionRegistryCache = reg
        return reg
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


}
