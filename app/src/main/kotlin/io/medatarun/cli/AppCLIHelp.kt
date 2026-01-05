package io.medatarun.cli

import io.medatarun.runtime.internal.AppRuntimeConfigFactory.Companion.MEDATARUN_APPLICATION_DATA_ENV
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AppCLIHelp(val reg: AppCLIActionRegistry) {

    fun printHelpRoot() {
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
        reg.findActionGroupNamesSorted().forEach { name ->
            logger.info("  help ${name}")
        }

    }

    fun printHelp(resource: String? = null, command: String? = null) {
        if (resource != null && command != null) {
            printHelpCommand(resource, command)
        } else if (resource != null) {
            printHelpResource(resource)
        } else {
            printHelpRoot()
        }
    }

    fun printHelpCommand(actionGroupKey: String, actionKey: String) {
        val actionGroup = reg.findActionGroup(actionGroupKey)
        if (actionGroup == null) {
            logger.error("Group not found: $actionGroupKey")
            return printHelpRoot()
        }
        val action = actionGroup.actions.find { it.actionKey == actionKey }
        if (action == null) {
            logger.error("Command not found: $actionGroupKey $actionKey")
            return printHelpResource(actionGroupKey)
        }



        logger.info("")
        logger.info("Group  : $actionGroupKey")
        logger.info("Command: $actionKey")
        logger.info("")
        action.title?.let { logger.info("  " + it) }
        logger.info("")
        action.description?.let { logger.info("  " + it) }
        logger.info("")
        val renderedParameters = action.parameters.joinToString("\n") { param ->
            "  --${param.key}=<${param.multiplatformType}>"

        }
        logger.info(renderedParameters)


    }

    fun printHelpResource(resourceId: String) {

        val resource = reg.findActionGroup(resourceId)
        if (resource == null) {
            logger.error("Group not found: $resourceId")
            printHelpRoot()
        } else {
            logger.info("Get help on available commands: help $resourceId <commandName>")
            val allCommands = resource.actions.sortedBy { it.actionKey.lowercase() }
            val maxKeySize = allCommands.map { it.actionKey }.maxByOrNull { it.length }?.length ?: 0
            allCommands.forEach { command ->
                logger.info(command.actionKey.padEnd(maxKeySize) + ": " + command.title?.ifBlank { "" })
            }
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AppCLIRunner::class.java)
    }
}