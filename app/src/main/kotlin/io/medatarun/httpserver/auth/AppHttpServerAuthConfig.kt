package io.medatarun.httpserver.auth

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.medatarun.auth.ports.exposed.OidcClientRegistrationResponseOrError
import io.medatarun.auth.ports.exposed.OidcService
import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory

fun Routing.installAuth(oidcService: OidcService) {

    val logger = LoggerFactory.getLogger("/register")

    post(oidcService.oidcRegisterUri()) {
        val body = call.receive<JsonObject>()
        logger.debug(body.toString())

        when (val result = oidcService.oidcRegister(body)) {
            is OidcClientRegistrationResponseOrError.Success -> {
                call.respond(HttpStatusCode.Created, result.registration)
            }

            is OidcClientRegistrationResponseOrError.Error -> {
                call.respond(HttpStatusCode.BadRequest, result)
            }
        }
    }

}
