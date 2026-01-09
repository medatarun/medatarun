package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.AuthCtx
import io.medatarun.auth.domain.Jwks
import io.medatarun.auth.domain.OIDCAuthorizeRequest
import io.medatarun.auth.domain.OIDCTokenRequest
import io.medatarun.auth.internal.AuthorizeResult
import kotlinx.serialization.json.JsonObject
import java.net.URI
import java.security.interfaces.RSAPublicKey

interface AuthEmbeddedOIDCService {
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
    fun oidcAuthorize(req: OIDCAuthorizeRequest): AuthorizeResult
    fun oidcAuthorizeFindAuthCtx(authCtxCode: String): AuthCtx?
    fun oidcAuthorizeCreateCode(authCtxCode: String, subject: String): String

    fun oidcTokenUri(): String
    fun oidcToken(oidcTokenReq: OIDCTokenRequest): OIDCTokenResponseOrError

    fun oidcAuthorizeErrorLocation(resp: AuthorizeResult.RedirectError): String



}