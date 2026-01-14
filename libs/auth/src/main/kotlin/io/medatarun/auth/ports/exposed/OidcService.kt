package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.jwt.Jwks
import io.medatarun.auth.domain.oidc.OidcAuthorizeCtx
import io.medatarun.auth.domain.oidc.OidcAuthorizeRequest
import io.medatarun.auth.domain.oidc.OidcTokenRequest
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

    /**
     * Provides a JWT verifier resolver so the HTTP layer can stay minimal and delegate
     * verification rules to the auth domain.
     */
    fun jwtVerifierResolver(): JwtVerifierResolver

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
