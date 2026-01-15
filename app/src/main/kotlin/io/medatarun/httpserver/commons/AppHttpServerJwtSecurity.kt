package io.medatarun.httpserver.commons

import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.httpserver.commons.AppHttpServerJwtSecurity.AUTH_MEDATARUN_JWT
import io.medatarun.lang.http.StatusCode
import io.medatarun.model.domain.MedatarunException

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
            verifier { header ->
                val token = extractBearerToken(header) ?: return@verifier null
                oidcService.jwtVerifierResolver().resolve(token)
            }
            validate { cred ->
                JWTPrincipal(cred.payload)
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "invalid or missing token")
            }
        }
    }
}

private fun extractBearerToken(header: HttpAuthHeader): String? {
    // Ktor exposes a single parsed Authorization header, not the raw list. If multiple Authorization headers
    // are sent, they may be merged or only the first retained depending on the server. Returning null keeps
    // this path as an auth failure (401 via challenge) instead of throwing and risking a 500. Detecting
    // multiple headers requires inspecting raw headers earlier in the pipeline.
    if (header !is HttpAuthHeader.Single) {
        return null
    }
    if (!header.authScheme.equals("Bearer", ignoreCase = true)) {
        return null
    }
    return header.blob
}

class JwtInvalidTokenException() : MedatarunException("Invalid Jwt token, must contain iss and sub.", StatusCode.UNAUTHORIZED)
