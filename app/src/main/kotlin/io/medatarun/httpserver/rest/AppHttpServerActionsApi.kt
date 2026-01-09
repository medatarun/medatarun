package io.medatarun.httpserver.rest

import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.medatarun.httpserver.commons.AppHttpServerJwtSecurity.AUTH_MEDATARUN_JWT
import io.medatarun.httpserver.commons.AppHttpServerTools.toMedatarunPrincipal

fun Routing.installActionsApi(restApiDoc: RestApiDoc, restCommandInvocation: RestCommandInvocation) {

    get("/api") {
        // Authentication: all public -> everybody needs to know API description
        call.respond(restApiDoc.buildApiDescription())
    }

    authenticate(AUTH_MEDATARUN_JWT) {
        route("/api/{actionGroupKey}/{actionKey}") {
            // Authentication: token required but not always, the action will check that principal
            // is present with correct roles the action require a principal, and not all actions need one
            // So we don't block actions if principal is missing
            get { restCommandInvocation.processInvocation(call, toMedatarunPrincipal(call)) }
            post { restCommandInvocation.processInvocation(call, toMedatarunPrincipal(call)) }
        }
    }
}