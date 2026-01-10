package io.medatarun.httpserver.commons

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.httpserver.commons.AppHttpServerJwtSecurity.AUTH_MEDATARUN_JWT

object AppHttpServerJwtSecurity {
    /**
     * Name of the security plugin that provides Jwt checks
     */
    const val AUTH_MEDATARUN_JWT = "medatarun-jwt"
}

/**
 * Note that there is NO endpoint to get OAuth tokens, this is done with
 * https://<medatarun>/api/auth/token which is an Action as all other actions.
 *
 * Only OIDC have specific endpoints
 */
fun Application.installJwtSecurity(oidcService: OidcService) {
    install(Authentication) {
        jwt(AUTH_MEDATARUN_JWT) {
            skipWhen { call ->
                call.request.headers[HttpHeaders.Authorization] == null
            }
            verifier(
                JWT.require(Algorithm.RSA256(oidcService.oidcPublicKey(), null))
                    .withIssuer(oidcService.oidcIssuer())
                    .withAudience(oidcService.oidcAudience())
                    .build()
            )
            validate { cred ->
                JWTPrincipal(cred.payload)
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "invalid or missing token")
            }
        }
    }
}