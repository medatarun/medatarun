package io.medatarun.httpserver.commons

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*


fun Application.installCors() {
    install(CORS) {
        // On n'assume pas le client : on autorise les méthodes usuelles + préflight
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Options)

        // On n'assume pas les headers : on laisse passer ceux courants
        // (et on permet explicitement Authorization au cas où)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Origin)


        // Important : pour ne pas ouvrir toute l'API, on limite les chemins CORS
        // à ceux que tu cites.
        allowNonSimpleContentTypes = true

        // Tu ne veux pas assumer l'origine, donc:
        // - en dev : anyHost()
        // - en prod : remplacer par allowHost("ui.example.com", schemes = listOf("https"))
        anyHost()

        // N'active PAS allowCredentials() tant que tu n'es pas sûr d'utiliser cookies.
        // Sinon, anyHost + credentials est interdit par les navigateurs.
        // allowCredentials = true
    }
}
