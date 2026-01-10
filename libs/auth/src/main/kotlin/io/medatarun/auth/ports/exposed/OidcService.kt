package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.Jwks
import io.medatarun.auth.domain.OidcAuthorizeCtx
import io.medatarun.auth.domain.OidcAuthorizeRequest
import io.medatarun.auth.domain.OidcTokenRequest
import io.medatarun.auth.internal.OidcAuthorizeResult
import kotlinx.serialization.json.JsonObject
import java.net.URI
import java.security.interfaces.RSAPublicKey

interface OidcService {
    fun oidcJwks(): Jwks
    fun oidcJwksUri(): String
    fun oidcPublicKey(): RSAPublicKey
    fun oidcIssuer(): String

    fun oidcAudience(): String


    fun oidcWellKnownOpenIdConfigurationUri(): String
    fun oidcWellKnownOpenIdConfiguration(publicBaseUrl: URI): JsonObject


    /**
     * Returns URL where authorize is done. Implementation shall return a URL where
     * users can put their login and password.
     */
    fun oidcAuthorizeUri(): String
    fun oidcAuthorize(req: OidcAuthorizeRequest): OidcAuthorizeResult
    fun oidcAuthorizeFindAuthCtx(authCtxCode: String): OidcAuthorizeCtx?
    fun oidcAuthorizeCreateCode(authCtxCode: String, subject: String): String

    fun oidcTokenUri(): String
    fun oidcToken(oidcTokenReq: OidcTokenRequest): OIDCTokenResponseOrError

    fun oidcAuthorizeErrorLocation(resp: OidcAuthorizeResult.RedirectError): String



}