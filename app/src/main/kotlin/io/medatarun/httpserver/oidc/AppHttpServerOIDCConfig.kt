package io.medatarun.httpserver.oidc

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.medatarun.auth.domain.oidc.OidcAuthorizeRequest
import io.medatarun.auth.domain.oidc.OidcTokenRequest
import io.medatarun.auth.internal.oidc.OidcAuthorizeResult
import io.medatarun.auth.ports.exposed.OIDCTokenResponseOrError
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.httpserver.oidc.OIDCAuthorizePage.Companion.PARAM_AUTH_CTX
import io.medatarun.httpserver.oidc.OIDCAuthorizePage.Companion.PARAM_PASSWORD
import io.medatarun.httpserver.oidc.OIDCAuthorizePage.Companion.PARAM_USERNAME
import java.net.URI

fun Routing.installOidc(oidcService: OidcService, userService: UserService, publicBaseUrl: URI) {

// ----------------------------------------------------------------
// OpenIdConnect
// ----------------------------------------------------------------

// Authentication: all public -> Jwks must be public for discovert
    get(oidcService.oidcJwksUri()) {
        call.respond(oidcService.oidcJwks())
    }

    get(oidcService.oidcWellKnownOpenIdConfigurationUri()) {
        call.respond(oidcService.oidcWellKnownOpenIdConfiguration(publicBaseUrl))
    }

    route(oidcService.oidcAuthorizeUri()) {
        suspend fun process(call: ApplicationCall) {
            // Displays webpage where user should authenticate himself (login/password)
            val req = OidcAuthorizeRequest(
                responseType = call.parameters["response_type"],
                clientId = call.parameters["client_id"],
                redirectUri = call.parameters["redirect_uri"],
                scope = call.parameters["scope"],
                state = call.parameters["state"],
                codeChallenge = call.parameters["code_challenge"],
                codeChallengeMethod = call.parameters["code_challenge_method"],
                nonce = call.parameters["nonce"]
            )

            val resp = oidcService.oidcAuthorize(req, publicBaseUrl)
            when (resp) {
                is OidcAuthorizeResult.FatalError -> {
                    call.respond(HttpStatusCode.BadRequest, resp.reason)
                }

                is OidcAuthorizeResult.RedirectError -> {
                    call.respondRedirect(oidcService.oidcAuthorizeErrorLocation(resp), false)
                }

                is OidcAuthorizeResult.Valid -> {
                    call.respondRedirect("/ui/auth/login?${PARAM_AUTH_CTX}=" + resp.authCtxCode, false)
                }
            }
        }
        get { process(call) }
        post { process(call) }
    }

    route("/ui/auth/login") {
        suspend fun handle(call: RoutingCall) {
            val params = when (call.request.httpMethod) {
                HttpMethod.Get -> call.parameters
                HttpMethod.Post -> call.receiveParameters()
                else -> Parameters.Empty
            }
            val authCtxCode = params[PARAM_AUTH_CTX]
            val username = params[PARAM_USERNAME]
            val password = params[PARAM_PASSWORD]


            val result = OIDCAuthorizePage(oidcService, userService).process(
                authCtxCode = authCtxCode,
                username = username,
                password = password
            )
            when (result) {
                is OIDCAuthorizePage.OIDCAuthorizePageResult.Fatal -> call.respondText(
                    status = HttpStatusCode.BadRequest,
                    contentType = ContentType.Text.Plain
                ) { result.message }

                is OIDCAuthorizePage.OIDCAuthorizePageResult.HtmlPage -> call.respondText(
                    status = HttpStatusCode.OK,
                    contentType = ContentType.Text.Html
                ) { result.body }

                is OIDCAuthorizePage.OIDCAuthorizePageResult.Redirect -> call.respondRedirect(
                    url = result.location,
                    permanent = false
                )
            }
        }
        get { handle(call) }
        post { handle(call) }
    }

    route(oidcService.oidcTokenUri()) {
        suspend fun handleOidcToken(call: RoutingCall) {
            val params = when (call.request.httpMethod) {
                HttpMethod.Get -> call.parameters
                HttpMethod.Post -> call.receiveParameters()
                else -> Parameters.Empty
            }
            // - échange authorization_code → id_token + access_token
            // - vérification PKCE
            // - émission d’un ID Token conforme OIDC
            // - signature RS256 avec ta clé persistante
            // - support refresh_token si annoncé


            fun process(): OIDCTokenResponseOrError {
                val request = OidcTokenRequest(
                    grantType = params["grant_type"]
                        ?: return OIDCTokenResponseOrError.Error("invalid_request", "grand_type"),
                    code = params["code"]
                        ?: return OIDCTokenResponseOrError.Error("invalid_request", "code"),
                    redirectUri = params["redirect_uri"]
                        ?: return OIDCTokenResponseOrError.Error("invalid_request", "redirect_uri"),
                    clientId = params["client_id"]
                        ?: return OIDCTokenResponseOrError.Error("invalid_request", "client_id"),
                    codeVerifier = params["code_verifier"]
                        ?: return OIDCTokenResponseOrError.Error("invalid_request", "code_verifier"),
                )
                return oidcService.oidcToken(request)
            }

            val tokenResponse = process()
            when (tokenResponse) {
                is OIDCTokenResponseOrError.Success -> call.respond(tokenResponse.token)
                is OIDCTokenResponseOrError.Error -> call.respond(tokenResponse)
            }

        }
        get() { handleOidcToken(call) }
        post() { handleOidcToken(call) }
    }

}