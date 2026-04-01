package io.medatarun.httpserver.oauth

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.medatarun.auth.ports.exposed.OidcService
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.net.URI

fun Routing.installOAuth(oidcService: OidcService, publicBaseUrl: URI) {

    // [RFC 9728] OAuth Protected Resource Metadata, defines this URL for
    // OAuth configuration discovery
    get("/.well-known/oauth-protected-resource") {

        // The resource to protect (this server instance)
        val resourceUrl = publicBaseUrl.toString()

        // Authorization server is the same as OIDC (for now). Otherwise,
        // it may be complicated for our users to understand or configure.
        // We could declare multiple servers, but MCP clients, in particular, are
        // quite new and not very strict on everything (including OpenAI ones)
        val authorizationServer = oidcService.oidcAuthority(publicBaseUrl).toString()

        val doc = buildJsonObject {
            put("resource", resourceUrl)
            putJsonArray("authorization_servers") {
                add(JsonPrimitive(authorizationServer))
            }
        }

        call.respond(doc)
    }

    // [RFC 8414] OAuth 2.0 Authorization Server Metadata defines this URL as
    // a mecanism for discovering features of our authorization server
    // The content of the JSON can be the same as the one of
    // our OIDC Configuration in /.well-known/openid-configuration
    // as OIDC just adds more information.
    // Note that for [RFC 7591] Dynamic client registration, the returned
    // document contains the "registration_endpoint".
    get("/.well-known/oauth-authorization-server") {
        call.respond(oidcService.oidcWellKnownOpenIdConfiguration(publicBaseUrl))
    }

    // We may later consider dividing resources but not sure for now
    // get("/.well-known/oauth-protected-resource/mcp") {
            // do quite the same thing
    // }
}