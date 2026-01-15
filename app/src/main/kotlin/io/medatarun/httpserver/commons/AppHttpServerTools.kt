package io.medatarun.httpserver.commons

import io.ktor.server.application.*
import java.util.*

object AppHttpServerTools {
    fun detectLocale(call: ApplicationCall): Locale {
        val header = call.request.headers["Accept-Language"]
        val firstTag = header
            ?.split(",")
            ?.map { it.substringBefore(";").trim() }
            ?.firstOrNull { it.isNotEmpty() }

        return firstTag?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
    }

}
