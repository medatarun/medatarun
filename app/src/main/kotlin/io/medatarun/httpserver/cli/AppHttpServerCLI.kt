package io.medatarun.httpserver.cli

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.medatarun.actions.runtime.ActionRegistry

/**
 * Specific APIs for CLI (getting online docs mostly)
 */
fun Routing.installCLI(actionRegistry: ActionRegistry) {
    get("/cli/api/action-registry") {
        // Authentication: actino registry for CLI is public (otherwise no help on CLI)
        call.respond(CliActionRegistry(actionRegistry).actionRegistryDto())
    }
}