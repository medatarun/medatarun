package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.oidc.OidcAuthorizeCtx
import io.medatarun.auth.domain.oidc.OidcAuthorizeRequest
import io.medatarun.auth.domain.oidc.OidcTokenRequest
import io.medatarun.auth.internal.oidc.OidcAuthorizeResult
import kotlinx.serialization.json.JsonObject
import java.net.URI

interface OidcService {
    /**
     * Returns Json object that matches RFC-7517 JSON Web Key (JWK)
     */
    fun oidcJwks(): JsonObject

    /**
     * URL relative to public base URL where the JWKS is located
     */
    fun oidcJwksUri(): String

    /**
     * Returns our issuer as configured.
     *
     * This is used in many parts to know "it is use who issued the key"
     *
     * Changes some behaviors in other parts of the system to distinguish
     * external sources (external IdP for example) from an internal source (our
     * internal IdP)
     */
    fun oidcIssuer(): String

    /**
     * Provides a JWT verifier resolver so the HTTP layer can stay minimal and delegate
     * verification rules to the auth domain.
     */
    fun jwtVerifierResolver(): JwtVerifierResolver

    /**
     * URI relative to public base URL to get the well-known openId configuration
     */
    fun oidcWellKnownOpenIdConfigurationUri(): String

    /**
     * Contents of well-known openid configuration in Json.
     * Must respect the standard
     */
    fun oidcWellKnownOpenIdConfiguration(publicBaseUrl: URI): JsonObject

    /**
     * Returns URL where authorize is done. Implementation shall return a URL where
     * users can put their login and password.
     */
    fun oidcAuthorizeUri(): String
    fun oidcAuthorize(req: OidcAuthorizeRequest, publicBaseUrl: URI): OidcAuthorizeResult
    fun oidcAuthorizeFindAuthCtx(authCtxCode: String): OidcAuthorizeCtx?
    fun oidcAuthorizeCreateCode(authCtxCode: String, subject: String): String

    fun oidcTokenUri(): String
    fun oidcToken(oidcTokenReq: OidcTokenRequest): OIDCTokenResponseOrError

    fun oidcAuthorizeErrorLocation(resp: OidcAuthorizeResult.RedirectError): String
    fun oidcAuthority(publicBaseUrl: URI): URI
    fun oidcClientId(): String


}
