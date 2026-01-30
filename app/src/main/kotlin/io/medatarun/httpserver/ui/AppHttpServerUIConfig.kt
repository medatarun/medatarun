package io.medatarun.httpserver.ui

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.medatarun.actions.runtime.ActionRegistry
import io.medatarun.httpserver.commons.AppHttpServerJwtSecurity.AUTH_MEDATARUN_JWT
import io.medatarun.httpserver.commons.AppHttpServerTools.detectLocale
import io.medatarun.model.domain.ModelId
import io.medatarun.runtime.AppRuntime
import java.net.URI

/**
 * Install Status pages and SPA fallback. It means
 *
 * - when you have a 404 not found
 * - and the path is NOT in the [noFallbackOn] neither those locally
 *
 * returns the index.html page (with templating)
 *
 * Why? because when users do something like `http://<medatarun>/models/123456`
 * we must let UI manage its own routes.
 *
 * Moreover, it's important in Medatarun that you can have permalinks on
 * models, entities, and attributes, because it's what the business users
 * may send to their devs when they want to speak about something.
 *
 */
fun Application.installUIStatusPageAndSpaFallback(
    uiIndexTemplate: UIIndexTemplate,
    noFallbackOn: List<String>,
    oidcAuthority: URI,
    oidcClientId: String,
) {

    // We add to the generic list of "noFallbackOn" our owns that we know
    val noFallbackOnWithLocals: List<String> = noFallbackOn + listOf(
        "/ui", "/assets", "/favicon"
    )

    install(StatusPages) {

        // what to do in case of 404 Not Found
        status(HttpStatusCode.NotFound) { call, status ->
            val path = call.request.path()

            // Here we are careful to not replace 404 coming from API, MCP, SSE or files
            if (noFallbackOnWithLocals.any{path.startsWith(it)} || path.contains('.')) {
                //call.respond(status)
                return@status
            }

            // Fallback to the UI home page, by making a template
            val index = javaClass.classLoader.getResource("static/index.html")
            if (index != null)
                call.respondText(uiIndexTemplate.render(index, oidcAuthority, oidcClientId), ContentType.Text.Html)
            else
                call.respond(status)
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
}

/**
 * Declares all resources that serve static assets
 */
fun Routing.installUIStaticResources() {
    // Authentication: all this is public
    staticResources("/assets", "static/assets")
    staticResources("/favicon", "static/favicon")
}

/**
 * Installs homepage to "/" so users have UI on https://<medatarun/ directly
 */
fun Routing.installUIHomepage(
    uiIndexTemplate: UIIndexTemplate,
    oidcAuthority: URI,
    oidcClientId: String,
) {
    get("/") {

        val index = javaClass.classLoader.getResource("static/index.html")
            ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respondText(uiIndexTemplate.render(index, oidcAuthority, oidcClientId), ContentType.Text.Html)

    }
}

/**
 * UI specific APIs, not meant for general API usages
 */
fun Routing.installUIApis(runtime: AppRuntime, actionRegistry: ActionRegistry) {

    get("/ui/api/action-registry") {
        // Authentication: the action registry for UI is public (otherwise no help on UI)
        call.respond(UI(runtime, actionRegistry).actionRegistryDto(detectLocale(call)))
    }

    authenticate(AUTH_MEDATARUN_JWT) {
        // Authentication: required
        get("/ui/api/models") {
            call.respondText(
                UI(runtime, actionRegistry).modelListJson(detectLocale(call)),
                ContentType.Application.Json
            )
        }
    }

    authenticate(AUTH_MEDATARUN_JWT) {
        get("/ui/api/models/{modelId}") {
            val modelId = call.parameters["modelId"] ?: throw NotFoundException()
            call.respondText(
                UI(runtime, actionRegistry).modelJson(ModelId.fromString(modelId), detectLocale(call)),
                ContentType.Application.Json
            )
        }
    }
}