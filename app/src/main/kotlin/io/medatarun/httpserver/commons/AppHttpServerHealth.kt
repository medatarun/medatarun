package io.medatarun.httpserver.commons

import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.installHealth() {

    // Authentication: all public -> otherwise UI can not load
    get("/health") {
        call.respond(mapOf("status" to "ok"))
    }
}