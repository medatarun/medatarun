package io.medatarun.httpserver.ui

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.URL

class UIIndexTemplate {
    fun render(index: URL): String {
        val html = index.readText()
        val json = buildJsonObject {
            put("toto", "tutu")
        }
        val replaced = html.replace("<!-- __SERVER_CONFIG__ -->", "<script>window.__MEDATARUN_CONFIG__=${json}</script>")
        return replaced
    }
}